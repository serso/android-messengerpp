package org.solovyev.android.messenger.accounts;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.Realms;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.accounts.AccountEventType.configuration_changed;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_app;
import static org.solovyev.android.messenger.accounts.AccountState.enabled;
import static org.solovyev.android.messenger.accounts.AccountState.removed;

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
	private RealmService realmService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private MessageService messageService;

	@Nonnull
	private final Object lock;

	@GuardedBy("accounts")
	@Nonnull
	private final Map<String, Account> accounts = new HashMap<String, Account>();

	@Nonnull
	private AtomicInteger accountCounter = new AtomicInteger(0);

	@Nonnull
	private final JEventListeners<JEventListener<? extends AccountEvent>, AccountEvent> listeners;

	@Inject
	public DefaultAccountService(@Nonnull PersistenceLock lock, @Nonnull Executor eventExecutor) {
		this.lock = lock;
		this.listeners = Listeners.newEventListenersBuilderFor(AccountEvent.class).withHardReferences().withExecutor(eventExecutor).create();
	}

	@Override
	public void init() {
		accountDao.init();
		userService.addListener(new UserEventListener());

		synchronized (lock) {
			// reset status to enabled for temporary disable realms
			for (Account account : accountDao.loadAccountsInState(disabled_by_app)) {
				changeAccountState(account, enabled, false);
			}

			// remove all scheduled to remove realms
			for (Account account : accountDao.loadAccountsInState(removed)) {
				this.accountDao.deleteById(account.getId());
				this.accounts.remove(account.getId());
			}

			for (final Realm<? extends AccountConfiguration> realm : realmService.getRealms()) {
				if (!realm.isEnabled()) {
					final Iterable<Account> accounts = filter(this.accounts.values(), new Predicate<Account>() {
						@Override
						public boolean apply(Account account) {
							return account.getRealm().equals(realm);
						}
					});

					for (Account account : accounts) {
						changeAccountState(account, disabled_by_app, false);
					}
				}
			}
		}
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

	@Nullable
	@Override
	public Account getAccountByIdOrNull(@Nonnull String accountId) {
		return this.accounts.get(accountId);
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
	public <A extends Account> A saveAccount(@Nonnull AccountBuilder<A> accountBuilder) throws InvalidCredentialsException, AccountAlreadyExistsException {
		A result;

		try {
			final AccountConfiguration configuration = accountBuilder.getConfiguration();
			final Account oldAccount = accountBuilder.getEditedAccount();
			final boolean sameCredentials = oldAccount != null && oldAccount.getConfiguration().isSameCredentials(configuration);
			if (sameCredentials) {
				// new account configuration is exactly the same => we need just to save new configuration
				updateAccountConfiguration(oldAccount, configuration);
				result = (A) oldAccount;
			} else {
				// saving account (account either new or changed)

				accountBuilder.connect();

				accountBuilder.loginUser(null);

				final String newAccountId;
				if (oldAccount != null) {
					newAccountId = oldAccount.getId();
				} else {
					newAccountId = generateAccountId(accountBuilder.getRealm());
				}
				final A newAccount = accountBuilder.build(new AccountBuilder.Data(newAccountId));

				synchronized (accounts) {
					final boolean alreadyExists = Iterables.any(accounts.values(), new Predicate<Account>() {
						@Override
						public boolean apply(@Nullable Account account) {
							return account != null && account.getState() != removed && newAccount.same(account);
						}
					});

					if (alreadyExists) {
						throw new AccountAlreadyExistsException();
					} else {
						createOrUpdateAccount(oldAccount, newAccount);
					}
				}

				result = newAccount;
			}
		} catch (AccountBuilder.ConnectionException e) {
			throw new InvalidCredentialsException(e);
		} catch (AccountException e) {
			App.getExceptionHandler().handleException(e);
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

	private void updateAccountConfiguration(@Nonnull Account account, @Nonnull AccountConfiguration newConfiguration) throws AccountException {
		assert account.getConfiguration().isSameAccount(newConfiguration);

		try {
			newConfiguration.applySystemData(account.getConfiguration());
			synchronized (lock) {
				account.setConfiguration(newConfiguration);
				accountDao.update(account);
				listeners.fireEvent(configuration_changed.newEvent(account, null));
			}
		} catch (AccountRuntimeException e) {
			throw new AccountException(e);
		}
	}

	private void createOrUpdateAccount(@Nullable Account oldAccount, @Nonnull Account newAccount) throws AccountException, InvalidCredentialsException {
		assert Thread.holdsLock(accounts);

		synchronized (lock) {
			try {
				if (oldAccount != null) {
					final User oldUser = oldAccount.getUser();
					final User newUser = newAccount.getUser();
					if (oldUser.equals(newUser)) {
						accountDao.update(newAccount);
						userService.saveUser(newAccount.getUser());
						accounts.put(newAccount.getId(), newAccount);
						listeners.fireEvent(AccountEventType.changed.newEvent(newAccount, null));
					} else {
						throw new InvalidCredentialsException("Account user has been changed: remove account and create new");
					}
				} else {
					accountDao.create(newAccount);
					userService.saveUser(newAccount.getUser());
					accounts.put(newAccount.getId(), newAccount);
					listeners.fireEvent(AccountEventType.created.newEvent(newAccount, null));
				}
			} catch (AccountRuntimeException e) {
				throw new AccountException(e);
			}
		}
	}

	@Nonnull
	@Override
	public Account changeAccountState(@Nonnull Account account, @Nonnull AccountState newState) {
		return changeAccountState(account, newState, true);
	}

	@Nonnull
	private Account changeAccountState(@Nonnull Account account, @Nonnull AccountState newState, boolean fireEvent) {
		if (account.getState() != newState) {
			try {
				final Account result = account.copyForNewState(newState);

				synchronized (accounts) {
					this.accounts.put(account.getId(), result);
					synchronized (lock) {
						this.accountDao.update(result);
					}
				}

				if (fireEvent) {
					listeners.fireEvent(AccountEventType.state_changed.newEvent(result, null));
				}

				return result;
			} catch (AccountRuntimeException e) {
				App.getExceptionHandler().handleException(e);
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
			changeAccountState(account, removed);
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
		return getAccountById(entity.getAccountId());
	}

	@Override
	public Account getAccountByEntityOrNull(@Nonnull Entity entity) {
		return getAccountByIdOrNull(entity.getAccountId());
	}

	@Nonnull
	@Override
	public Account getAccountByEntityAware(@Nonnull EntityAware entityAware) throws UnsupportedAccountException {
		return getAccountByEntity(entityAware.getEntity());
	}

	@Nonnull
	private String generateAccountId(@Nonnull Realm realm) {
		return Realms.makeAccountId(realm.getId(), accountCounter.getAndIncrement());
	}

	@Override
	public void load() {
		final Collection<Account> realmsFromDb = accountDao.readAll();
		synchronized (accounts) {
			int maxRealmIndex = 0;

			accounts.clear();
			for (Account account : realmsFromDb) {
				final String realmId = account.getId();
				accounts.put(realmId, account);

				// +1 for '~' symbol between realm and index
				String realmIndexString = realmId.substring(account.getRealm().getId().length() + 1);
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
			return getAccountById(user.getEntity().getAccountId()).getRealm().getUserProperties(user, context);
		} catch (UnsupportedAccountException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean canCreateUsers() {
		return any(getEnabledAccounts(), new CanCreateUserPredicate());
	}

	@Nonnull
	@Override
	public Collection<Account> getAccountsCreatingUsers() {
		return newArrayList(filter(getEnabledAccounts(), new CanCreateUserPredicate()));
	}

	private static class CanCreateUserPredicate implements Predicate<Account> {
		@Override
		public boolean apply(@Nullable Account account) {
			return account != null && account.getRealm().canCreateUsers();
		}
	}

	private final class UserEventListener extends AbstractJEventListener<UserEvent> {

		public UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			switch (event.getType()) {
				case changed:
					final User user = event.getUser();
					for (Account account : getAccounts()) {
						if(account.getUser().equals(user)) {
							account.setUser(user);
						}
					}
					break;
			}
		}
	}
}
