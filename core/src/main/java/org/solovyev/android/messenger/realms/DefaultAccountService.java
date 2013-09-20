package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:02 AM
 */
@Singleton
public class DefaultAccountService implements AccountService {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@GuardedBy("lock")
	@Inject
	@Nonnull
	private AccountDao accountDao;

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private ChatMessageService messageService;

	@Nonnull
	private final Object lock;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/
	@Nonnull
	private final Context context;

	@Nonnull
	private final Map<String, RealmDef<? extends AccountConfiguration>> realmDefs = new HashMap<String, RealmDef<? extends AccountConfiguration>>();

	@GuardedBy("realms")
	@Nonnull
	private final Map<String, Account> accounts = new HashMap<String, Account>();

	@Nonnull
	private AtomicInteger accountCounter = new AtomicInteger(0);

	@Nonnull
	private final JEventListeners<JEventListener<? extends AccountEvent>, AccountEvent> listeners;

	@Inject
	public DefaultAccountService(@Nonnull Application context, @Nonnull MessengerConfiguration configuration, @Nonnull PersistenceLock lock, @Nonnull ExecutorService eventExecutor) {
		this(context, configuration.getRealmDefs(), lock, eventExecutor);
	}

	public DefaultAccountService(@Nonnull Application context, @Nonnull Collection<? extends RealmDef> realmDefs, @Nonnull PersistenceLock lock, @Nonnull ExecutorService eventExecutor) {
		for (RealmDef realmDef : realmDefs) {
			this.realmDefs.put(realmDef.getId(), realmDef);
		}

		this.context = context;
		this.lock = lock;
		this.listeners = Listeners.newEventListenersBuilderFor(AccountEvent.class).withHardReferences().withExecutor(eventExecutor).create();
	}

	@Override
	public void init() {
		accountDao.init();

		for (RealmDef realmDef : realmDefs.values()) {
			realmDef.init(context);
		}

		synchronized (lock) {
			// reset status to enabled for temporary disable realms
			for (Account account : accountDao.loadRealmsInState(AccountState.disabled_by_app)) {
				changeRealmState(account, AccountState.enabled, false);
			}

			// remove all scheduled to remove realms
			for (Account account : accountDao.loadRealmsInState(AccountState.removed)) {
				this.messageService.removeAllMessagesInRealm(account.getId());
				this.chatService.removeChatsInRealm(account.getId());
				this.userService.removeUsersInRealm(account.getId());
				this.accountDao.deleteRealm(account.getId());
				this.accounts.remove(account.getId());
			}
		}
	}

	@Nonnull
	@Override
	public Collection<RealmDef<? extends AccountConfiguration>> getRealmDefs() {
		return Collections.unmodifiableCollection(this.realmDefs.values());
	}

	@Nonnull
	@Override
	public Collection<Account> getAccounts() {
		synchronized (this.accounts) {
			// must copy as concurrent modification might occur (e.g. realm removal)
			return new ArrayList<Account>(this.accounts.values());
		}
	}

	@Nonnull
	@Override
	public Collection<Account> getEnabledAccounts() {
		synchronized (this.accounts) {
			final List<Account> result = new ArrayList<Account>(this.accounts.size());
			for (Account account : this.accounts.values()) {
				if (account.isEnabled()) {
					result.add(account);
				}
			}
			return result;
		}
	}

	@Nonnull
	@Override
	public Collection<User> getEnabledAccountUsers() {
		final List<User> result = new ArrayList<User>();

		for (Account account : getEnabledAccounts()) {
			result.add(account.getUser());
		}

		return result;
	}

	@Nonnull
	@Override
	public Collection<User> getAccountUsers() {
		final List<User> result = new ArrayList<User>();
		synchronized (this.accounts) {
			for (Account account : this.accounts.values()) {
				result.add(account.getUser());
			}
		}
		return result;
	}

	@Nonnull
	@Override
	public RealmDef<? extends AccountConfiguration> getRealmDefById(@Nonnull String realmDefId) throws UnsupportedAccountException {
		final RealmDef<? extends AccountConfiguration> realm = this.realmDefs.get(realmDefId);
		if (realm == null) {
			throw new UnsupportedAccountException(realmDefId);
		}
		return realm;
	}

	@Nonnull
	@Override
	public Account getAccountById(@Nonnull String accountId) throws UnsupportedAccountException {
		final Account account = this.accounts.get(accountId);
		if (account == null) {
			throw new UnsupportedAccountException(accountId);
		}
		return account;
	}

	@Nonnull
	@Override
	public Account saveAccount(@Nonnull AccountBuilder accountBuilder) throws InvalidCredentialsException, AccountAlreadyExistsException {
		Account result;

		try {
			final AccountConfiguration configuration = accountBuilder.getConfiguration();
			final Account oldAccount = accountBuilder.getEditedAccount();
			if (oldAccount != null && oldAccount.getConfiguration().equals(configuration)) {
				// new realm configuration is exactly the same => can omit saving the realm
				result = oldAccount;
			} else {
				// saving realm (realm either new or changed)

				accountBuilder.connect();

				accountBuilder.loginUser(null);

				final String newRealmId;
				if (oldAccount != null) {
					newRealmId = oldAccount.getId();
				} else {
					newRealmId = generateRealmId(accountBuilder.getRealmDef());
				}
				final Account newAccount = accountBuilder.build(new AccountBuilder.Data(newRealmId));

				synchronized (accounts) {
					final boolean alreadyExists = Iterables.any(accounts.values(), new Predicate<Account>() {
						@Override
						public boolean apply(@Nullable Account realm) {
							return realm != null && realm.getState() != AccountState.removed && newAccount.same(realm);
						}
					});

					if (alreadyExists) {
						throw new AccountAlreadyExistsException();
					} else {
						synchronized (lock) {
							if (oldAccount != null) {
								accountDao.updateRealm(newAccount);
								accounts.put(newAccount.getId(), newAccount);
								listeners.fireEvent(AccountEventType.changed.newEvent(newAccount, null));
							} else {
								accountDao.insertRealm(newAccount);
								accounts.put(newAccount.getId(), newAccount);
								listeners.fireEvent(AccountEventType.created.newEvent(newAccount, null));
							}
						}
					}
				}

				result = newAccount;
			}
		} catch (AccountBuilder.ConnectionException e) {
			throw new InvalidCredentialsException(e);
		} catch (AccountException e) {
			MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			throw new InvalidCredentialsException(e);
		} finally {
			try {
				accountBuilder.disconnect();
			} catch (AccountBuilder.ConnectionException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public Account changeAccountState(@Nonnull Account account, @Nonnull AccountState newState) {
		return changeRealmState(account, newState, true);
	}

	@Nonnull
	private Account changeRealmState(@Nonnull Account account, @Nonnull AccountState newState, boolean fireEvent) {
		if (account.getState() != newState) {
			try {
				final Account result = account.copyForNewState(newState);

				synchronized (accounts) {
					this.accounts.put(account.getId(), result);
					synchronized (lock) {
						this.accountDao.updateRealm(result);
					}
				}

				if (fireEvent) {
					listeners.fireEvent(AccountEventType.state_changed.newEvent(result, null));
				}

				return result;
			} catch (AccountException e) {
				MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
				// return old unchanged value in case of error
				return account;
			}
		} else {
			return account;
		}
	}

	@Override
	public void removeAccount(@Nonnull String accountId) {
		final Account account;

		synchronized (accounts) {
			account = this.accounts.get(accountId);
		}

		if (account != null) {
			changeAccountState((Account) account, (AccountState) AccountState.removed);
		}
	}

	@Override
	public boolean isOneAccount() {
		synchronized (accounts) {
			return accounts.size() == 1;
		}
	}

	@Nonnull
	@Override
	public Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return getAccountById(entity.getRealmId());
	}

	@Nonnull
	@Override
	public Account getAccountByEntityAware(@Nonnull EntityAware entityAware) throws UnsupportedAccountException {
		return getAccountByEntity(entityAware.getEntity());
	}

	@Nonnull
	private String generateRealmId(@Nonnull RealmDef realmDef) {
		return EntityImpl.getRealmId(realmDef.getId(), accountCounter.getAndIncrement());
	}

	@Override
	public void load() {
		final Collection<Account> realmsFromDb = accountDao.loadRealms();
		synchronized (accounts) {
			int maxRealmIndex = 0;

			accounts.clear();
			for (Account account : realmsFromDb) {
				final String realmId = account.getId();
				accounts.put(realmId, account);

				// +1 for '~' symbol between realm and index
				String realmIndexString = realmId.substring(account.getRealmDef().getId().length() + 1);
				try {
					maxRealmIndex = Math.max(Integer.valueOf(realmIndexString), maxRealmIndex);
				} catch (NumberFormatException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			accountCounter.set(maxRealmIndex + 1);
		}
	}

	@Override
	public void addListener(@Nonnull JEventListener<AccountEvent> listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(@Nonnull JEventListener<AccountEvent> listener) {
		listeners.removeListener(listener);
	}

	@Override
	public void stopAllRealmConnections() {
		for (Account account : getAccounts()) {
			listeners.fireEvent(AccountEventType.stop.newEvent(account, null));
		}
	}

	@Override
	public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
		try {
			return getAccountById(user.getEntity().getRealmId()).getRealmDef().getUserProperties(user, context);
		} catch (UnsupportedAccountException e) {
			return Collections.emptyList();
		}
	}
}
