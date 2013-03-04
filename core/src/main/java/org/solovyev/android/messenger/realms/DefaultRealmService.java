package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    @Inject
    @Nonnull
    private RealmDao realmDao;

    @Inject
    @Nonnull
    private UserService userService;

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
    private final Map<String, RealmDef> realmDefs = new HashMap<String, RealmDef>();

    @Nonnull
    private final Map<String, Realm> realms = new HashMap<String, Realm>();

    @Nonnull
    private AtomicInteger realmCounter = new AtomicInteger(0);

    @Nonnull
    private final JEventListeners<JEventListener<? extends RealmEvent>, RealmEvent> listeners;

    @Inject
    public DefaultRealmService(@Nonnull Application context, @Nonnull MessengerConfiguration configuration) {
        this(context, configuration.getRealmDefs());
    }

    public DefaultRealmService(@Nonnull Application context, @Nonnull Collection<? extends RealmDef> realmDefs) {
        for (RealmDef realmDef : realmDefs) {
            this.realmDefs.put(realmDef.getId(), realmDef);
        }

        this.context = context;
        this.listeners = Listeners.newEventListenersBuilderFor(RealmEvent.class).withHardReferences().onBackgroundThread().create();
    }

    @Override
    public void init() {
        for (RealmDef realmDef : realmDefs.values()) {
            realmDef.init(context);
        }
    }

    @Nonnull
    @Override
    public Collection<RealmDef> getRealmDefs() {
        return Collections.unmodifiableCollection(this.realmDefs.values());
    }

    @Nonnull
    @Override
    public Collection<Realm> getRealms() {
        return Collections.unmodifiableCollection(this.realms.values());
    }

    @Nonnull
    @Override
    public RealmDef getRealmDefById(@Nonnull String realmDefId) throws UnsupportedRealmException {
        final RealmDef realm = this.realmDefs.get(realmDefId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmDefId);
        }
        return realm;
    }

    @Nonnull
    @Override
    public Realm getRealmById(@Nonnull String realmId) throws UnsupportedRealmException {
        final Realm realm = this.realms.get(realmId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmId);
        }
        return realm;
    }

    @Nonnull
    @Override
    public Realm saveRealm(@Nonnull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException {
        Realm result;

        try {
            final RealmConfiguration configuration = realmBuilder.getConfiguration();
            final Realm oldRealm = realmBuilder.getEditedRealm();
            if ( oldRealm != null && oldRealm.getConfiguration().equals(configuration) ) {
                // new realm configuration is exactly the same => can omit saving the realm
                result = oldRealm;
            } else {
                // saving realm (realm either new or changed)

                realmBuilder.connect();

                final AuthData authData = realmBuilder.loginUser(null);

                final String newRealmId;
                if ( oldRealm != null ) {
                    newRealmId = oldRealm.getId();
                } else {
                    newRealmId = generateRealmId(realmBuilder.getRealmDef());
                }
                final Realm newRealm = realmBuilder.build(new RealmBuilder.Data(authData, newRealmId));

                synchronized (realms) {
                    final boolean alreadyExists = Iterables.any(realms.values(), new Predicate<Realm>() {
                        @Override
                        public boolean apply(@Nullable Realm realm) {
                            return realm != null && newRealm.same(realm);
                        }
                    });

                    if (alreadyExists) {
                        throw new RealmAlreadyExistsException();
                    } else {
                        if (oldRealm != null) {
                            realmDao.updateRealm(newRealm);
                            realms.put(newRealm.getId(), newRealm);
                            listeners.fireEvent(new RealmChangedEvent(newRealm));
                        } else {
                            realmDao.insertRealm(newRealm);
                            realms.put(newRealm.getId(), newRealm);
                            listeners.fireEvent(new RealmAddedEvent(newRealm));

                        }
                    }
                }

                result = newRealm;
            }
        } catch (RealmBuilder.ConnectionException e) {
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

    @Override
    public void removeRealm(@Nonnull String realmId) {
        synchronized (realms) {
            final Realm realm = this.realms.get(realmId);
                if (realm != null) {
                this.userService.removeUsersInRealm(realmId);
                this.realmDao.deleteRealm(realmId);
                this.realms.remove(realmId);

                listeners.fireEvent(new RealmRemovedEvent(realm));
            }
        }
    }

    @Nonnull
    private String generateRealmId(@Nonnull RealmDef realmDef) {
        return RealmEntityImpl.getRealmId(realmDef.getId(), realmCounter.getAndIncrement());
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
    public void addListener(@Nonnull JEventListener<? extends RealmEvent> listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeListener(@Nonnull JEventListener<? extends RealmEvent> listener) {
        listeners.removeListener(listener);
    }
}
