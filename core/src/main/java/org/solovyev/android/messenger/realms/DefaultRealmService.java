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
	private final Map<String, Realm> realms = new HashMap<String, Realm>();

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
			for (Realm realm : realmDao.loadRealmsInState(RealmState.disabled_by_app)) {
				changeRealmState(realm, RealmState.enabled, false);
			}

			// remove all scheduled to remove realms
			for (Realm realm : realmDao.loadRealmsInState(RealmState.removed)) {
				this.messageService.removeAllMessagesInRealm(realm.getId());
				this.chatService.removeChatsInRealm(realm.getId());
				this.userService.removeUsersInRealm(realm.getId());
				this.realmDao.deleteRealm(realm.getId());
				this.realms.remove(realm.getId());
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
	public Collection<Realm> getRealms() {
		synchronized (this.realms) {
			// must copy as concurrent modification might occur (e.g. realm removal)
			return new ArrayList<Realm>(this.realms.values());
		}
	}

	@Nonnull
	@Override
	public Collection<Realm> getEnabledRealms() {
		synchronized (this.realms) {
			final List<Realm> result = new ArrayList<Realm>(this.realms.size());
			for (Realm realm : this.realms.values()) {
				if (realm.isEnabled()) {
					result.add(realm);
				}
			}
			return result;
		}
	}

	@Nonnull
	@Override
	public Collection<User> getEnabledRealmUsers() {
		final List<User> result = new ArrayList<User>();

		for (Realm realm : getEnabledRealms()) {
			result.add(realm.getUser());
		}

		return result;
	}

	@Nonnull
	@Override
	public Collection<User> getRealmUsers() {
		final List<User> result = new ArrayList<User>();
		synchronized (this.realms) {
			for (Realm realm : this.realms.values()) {
				result.add(realm.getUser());
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
	public Realm getRealmById(@Nonnull String realmId) throws UnsupportedRealmException {
		final Realm realm = this.realms.get(realmId);
		if (realm == null) {
			throw new UnsupportedRealmException(realmId);
		}
		return realm;
	}

	@Nonnull
	@Override
	public Realm saveRealm(@Nonnull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException {
		Realm result;

		try {
			final AccountConfiguration configuration = realmBuilder.getConfiguration();
			final Realm oldRealm = realmBuilder.getEditedRealm();
			if (oldRealm != null && oldRealm.getConfiguration().equals(configuration)) {
				// new realm configuration is exactly the same => can omit saving the realm
				result = oldRealm;
			} else {
				// saving realm (realm either new or changed)

				realmBuilder.connect();

				realmBuilder.loginUser(null);

				final String newRealmId;
				if (oldRealm != null) {
					newRealmId = oldRealm.getId();
				} else {
					newRealmId = generateRealmId(realmBuilder.getRealmDef());
				}
				final Realm newRealm = realmBuilder.build(new RealmBuilder.Data(newRealmId));

				synchronized (realms) {
					final boolean alreadyExists = Iterables.any(realms.values(), new Predicate<Realm>() {
						@Override
						public boolean apply(@Nullable Realm realm) {
							return realm != null && realm.getState() != RealmState.removed && newRealm.same(realm);
						}
					});

					if (alreadyExists) {
						throw new RealmAlreadyExistsException();
					} else {
						synchronized (lock) {
							if (oldRealm != null) {
								realmDao.updateRealm(newRealm);
								realms.put(newRealm.getId(), newRealm);
								listeners.fireEvent(RealmEventType.changed.newEvent(newRealm, null));
							} else {
								realmDao.insertRealm(newRealm);
								realms.put(newRealm.getId(), newRealm);
								listeners.fireEvent(RealmEventType.created.newEvent(newRealm, null));
							}
						}
					}
				}

				result = newRealm;
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
	public Realm changeRealmState(@Nonnull Realm realm, @Nonnull RealmState newState) {
		return changeRealmState(realm, newState, true);
	}

	@Nonnull
	private Realm changeRealmState(@Nonnull Realm realm, @Nonnull RealmState newState, boolean fireEvent) {
		if (realm.getState() != newState) {
			try {
				final Realm result = realm.copyForNewState(newState);

				synchronized (realms) {
					this.realms.put(realm.getId(), result);
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
				return realm;
			}
		} else {
			return realm;
		}
	}

	@Override
	public void removeRealm(@Nonnull String realmId) {
		final Realm realm;

		synchronized (realms) {
			realm = this.realms.get(realmId);
		}

		if (realm != null) {
			changeRealmState((Realm) realm, (RealmState) RealmState.removed);
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
	public Realm getRealmByEntity(@Nonnull Entity entity) throws UnsupportedRealmException {
		return getRealmById(entity.getRealmId());
	}

	@Nonnull
	@Override
	public Realm getRealmByEntityAware(@Nonnull EntityAware entityAware) throws UnsupportedRealmException {
		return getRealmByEntity(entityAware.getEntity());
	}

	@Nonnull
	private String generateRealmId(@Nonnull RealmDef realmDef) {
		return EntityImpl.getRealmId(realmDef.getId(), realmCounter.getAndIncrement());
	}

	@Override
	public void load() {
		final Collection<Realm> realmsFromDb = realmDao.loadRealms();
		synchronized (realms) {
			int maxRealmIndex = 0;

			realms.clear();
			for (Realm realm : realmsFromDb) {
				final String realmId = realm.getId();
				realms.put(realmId, realm);

				// +1 for '~' symbol between realm and index
				String realmIndexString = realmId.substring(realm.getRealmDef().getId().length() + 1);
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
		for (Realm realm : getRealms()) {
			listeners.fireEvent(RealmEventType.stop.newEvent(realm, null));
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
