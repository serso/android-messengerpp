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
public class DefaultRealmService implements RealmService {

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
	private RealmDao realmDao;

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
	private final Map<String, Account> realms = new HashMap<String, Account>();

	@Nonnull
	private AtomicInteger realmCounter = new AtomicInteger(0);

	@Nonnull
	private final JEventListeners<JEventListener<? extends RealmEvent>, RealmEvent> listeners;

	@Inject
	public DefaultRealmService(@Nonnull Application context, @Nonnull MessengerConfiguration configuration, @Nonnull PersistenceLock lock, @Nonnull ExecutorService eventExecutor) {
		this(context, configuration.getRealmDefs(), lock, eventExecutor);
	}

	public DefaultRealmService(@Nonnull Application context, @Nonnull Collection<? extends RealmDef> realmDefs, @Nonnull PersistenceLock lock, @Nonnull ExecutorService eventExecutor) {
		for (RealmDef realmDef : realmDefs) {
			this.realmDefs.put(realmDef.getId(), realmDef);
		}

		this.context = context;
		this.lock = lock;
		this.listeners = Listeners.newEventListenersBuilderFor(RealmEvent.class).withHardReferences().withExecutor(eventExecutor).create();
	}

	@Override
	public void init() {
		realmDao.init();

		for (RealmDef realmDef : realmDefs.values()) {
			realmDef.init(context);
		}

		synchronized (lock) {
			// reset status to enabled for temporary disable realms
			for (Account account : realmDao.loadRealmsInState(AccountState.disabled_by_app)) {
				changeRealmState(account, AccountState.enabled, false);
			}

			// remove all scheduled to remove realms
			for (Account account : realmDao.loadRealmsInState(AccountState.removed)) {
				this.messageService.removeAllMessagesInRealm(account.getId());
				this.chatService.removeChatsInRealm(account.getId());
				this.userService.removeUsersInRealm(account.getId());
				this.realmDao.deleteRealm(account.getId());
				this.realms.remove(account.getId());
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
	public Collection<Account> getRealms() {
		synchronized (this.realms) {
			// must copy as concurrent modification might occur (e.g. realm removal)
			return new ArrayList<Account>(this.realms.values());
		}
	}

	@Nonnull
	@Override
	public Collection<Account> getEnabledRealms() {
		synchronized (this.realms) {
			final List<Account> result = new ArrayList<Account>(this.realms.size());
			for (Account account : this.realms.values()) {
				if (account.isEnabled()) {
					result.add(account);
				}
			}
			return result;
		}
	}

	@Nonnull
	@Override
	public Collection<User> getEnabledRealmUsers() {
		final List<User> result = new ArrayList<User>();

		for (Account account : getEnabledRealms()) {
			result.add(account.getUser());
		}

		return result;
	}

	@Nonnull
	@Override
	public Collection<User> getRealmUsers() {
		final List<User> result = new ArrayList<User>();
		synchronized (this.realms) {
			for (Account account : this.realms.values()) {
				result.add(account.getUser());
			}
		}
		return result;
	}

	@Nonnull
	@Override
	public RealmDef<? extends AccountConfiguration> getRealmDefById(@Nonnull String realmDefId) throws UnsupportedRealmException {
		final RealmDef<? extends AccountConfiguration> realm = this.realmDefs.get(realmDefId);
		if (realm == null) {
			throw new UnsupportedRealmException(realmDefId);
		}
		return realm;
	}

	@Nonnull
	@Override
	public Account getRealmById(@Nonnull String realmId) throws UnsupportedRealmException {
		final Account account = this.realms.get(realmId);
		if (account == null) {
			throw new UnsupportedRealmException(realmId);
		}
		return account;
	}

	@Nonnull
	@Override
	public Account saveRealm(@Nonnull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException {
		Account result;

		try {
			final AccountConfiguration configuration = realmBuilder.getConfiguration();
			final Account oldAccount = realmBuilder.getEditedAccount();
			if (oldAccount != null && oldAccount.getConfiguration().equals(configuration)) {
				// new realm configuration is exactly the same => can omit saving the realm
				result = oldAccount;
			} else {
				// saving realm (realm either new or changed)

				realmBuilder.connect();

				realmBuilder.loginUser(null);

				final String newRealmId;
				if (oldAccount != null) {
					newRealmId = oldAccount.getId();
				} else {
					newRealmId = generateRealmId(realmBuilder.getRealmDef());
				}
				final Account newAccount = realmBuilder.build(new RealmBuilder.Data(newRealmId));

				synchronized (realms) {
					final boolean alreadyExists = Iterables.any(realms.values(), new Predicate<Account>() {
						@Override
						public boolean apply(@Nullable Account realm) {
							return realm != null && realm.getState() != AccountState.removed && newAccount.same(realm);
						}
					});

					if (alreadyExists) {
						throw new RealmAlreadyExistsException();
					} else {
						synchronized (lock) {
							if (oldAccount != null) {
								realmDao.updateRealm(newAccount);
								realms.put(newAccount.getId(), newAccount);
								listeners.fireEvent(RealmEventType.changed.newEvent(newAccount, null));
							} else {
								realmDao.insertRealm(newAccount);
								realms.put(newAccount.getId(), newAccount);
								listeners.fireEvent(RealmEventType.created.newEvent(newAccount, null));
							}
						}
					}
				}

				result = newAccount;
			}
		} catch (RealmBuilder.ConnectionException e) {
			throw new InvalidCredentialsException(e);
		} catch (RealmException e) {
			MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			throw new InvalidCredentialsException(e);
		} finally {
			try {
				realmBuilder.disconnect();
			} catch (RealmBuilder.ConnectionException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public Account changeRealmState(@Nonnull Account account, @Nonnull AccountState newState) {
		return changeRealmState(account, newState, true);
	}

	@Nonnull
	private Account changeRealmState(@Nonnull Account account, @Nonnull AccountState newState, boolean fireEvent) {
		if (account.getState() != newState) {
			try {
				final Account result = account.copyForNewState(newState);

				synchronized (realms) {
					this.realms.put(account.getId(), result);
					synchronized (lock) {
						this.realmDao.updateRealm(result);
					}
				}

				if (fireEvent) {
					listeners.fireEvent(RealmEventType.state_changed.newEvent(result, null));
				}

				return result;
			} catch (RealmException e) {
				MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
				// return old unchanged value in case of error
				return account;
			}
		} else {
			return account;
		}
	}

	@Override
	public void removeRealm(@Nonnull String realmId) {
		final Account account;

		synchronized (realms) {
			account = this.realms.get(realmId);
		}

		if (account != null) {
			changeRealmState((Account) account, (AccountState) AccountState.removed);
		}
	}

	@Override
	public boolean isOneRealm() {
		synchronized (realms) {
			return realms.size() == 1;
		}
	}

	@Nonnull
	@Override
	public Account getRealmByEntity(@Nonnull Entity entity) throws UnsupportedRealmException {
		return getRealmById(entity.getRealmId());
	}

	@Nonnull
	@Override
	public Account getRealmByEntityAware(@Nonnull EntityAware entityAware) throws UnsupportedRealmException {
		return getRealmByEntity(entityAware.getEntity());
	}

	@Nonnull
	private String generateRealmId(@Nonnull RealmDef realmDef) {
		return EntityImpl.getRealmId(realmDef.getId(), realmCounter.getAndIncrement());
	}

	@Override
	public void load() {
		final Collection<Account> realmsFromDb = realmDao.loadRealms();
		synchronized (realms) {
			int maxRealmIndex = 0;

			realms.clear();
			for (Account account : realmsFromDb) {
				final String realmId = account.getId();
				realms.put(realmId, account);

				// +1 for '~' symbol between realm and index
				String realmIndexString = realmId.substring(account.getRealmDef().getId().length() + 1);
				try {
					maxRealmIndex = Math.max(Integer.valueOf(realmIndexString), maxRealmIndex);
				} catch (NumberFormatException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			realmCounter.set(maxRealmIndex + 1);
		}
	}

	@Override
	public void addListener(@Nonnull JEventListener<RealmEvent> listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(@Nonnull JEventListener<RealmEvent> listener) {
		listeners.removeListener(listener);
	}

	@Override
	public void stopAllRealmConnections() {
		for (Account account : getRealms()) {
			listeners.fireEvent(RealmEventType.stop.newEvent(account, null));
		}
	}

	@Override
	public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
		try {
			return getRealmById(user.getEntity().getRealmId()).getRealmDef().getUserProperties(user, context);
		} catch (UnsupportedRealmException e) {
			return Collections.emptyList();
		}
	}
}
