package org.solovyev.android.messenger.realms;

import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

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

    @Inject
    @NotNull
    private RealmDao realmDao;

    @NotNull
    private final Map<String, RealmDef> realmDefs = new HashMap<String, RealmDef>();

    @NotNull
    private final Map<String, Realm> realms = new HashMap<String, Realm>();

    @NotNull
    private AtomicInteger realmCounter = new AtomicInteger(0);

    @NotNull
    private final JEventListeners<JEventListener<? extends RealmEvent>, RealmEvent> listeners;

    @Inject
    public DefaultRealmService(@NotNull MessengerConfiguration configuration) {
        this(configuration.getRealmDefs());
    }

    public DefaultRealmService(@NotNull Collection<? extends RealmDef> realmDefs) {
        for (RealmDef realmDef : realmDefs) {
            this.realmDefs.put(realmDef.getId(), realmDef);
        }

        listeners = Listeners.newEventListenersBuilderFor(RealmEvent.class).withHardReferences().onBackgroundThread().create();
    }

    @NotNull
    @Override
    public Collection<RealmDef> getRealmDefs() {
        return Collections.unmodifiableCollection(this.realmDefs.values());
    }

    @NotNull
    @Override
    public Collection<Realm> getRealms() {
        return Collections.unmodifiableCollection(this.realms.values());
    }

    @NotNull
    @Override
    public RealmDef getRealmDefById(@NotNull String realmDefId) throws UnsupportedRealmException {
        final RealmDef realm = this.realmDefs.get(realmDefId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmDefId);
        }
        return realm;
    }

    @NotNull
    @Override
    public Realm getRealmById(@NotNull String realmId) throws UnsupportedRealmException {
        final Realm realm = this.realms.get(realmId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmId);
        }
        return realm;
    }

    @Override
    public void addRealm(@NotNull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException {
        try {
            realmBuilder.connect();

            final AuthData authData = realmBuilder.loginUser(null);

            final Realm newRealm = realmBuilder.build(new RealmBuilder.Data(authData, generateRealmId(realmBuilder.getRealmDef())));
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
                    realmDao.insertRealm(newRealm);
                    realms.put(newRealm.getId(), newRealm);
                }
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
    }

    @NotNull
    private String generateRealmId(@NotNull RealmDef realmDef) {
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

                String realmIndexString = realmId.substring(0, realm.getRealmDef().getId().length());
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
    public void addListener(@NotNull JEventListener<? extends RealmEvent> listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeListener(@NotNull JEventListener<? extends RealmEvent> listener) {
        listeners.removeListener(listener);
    }
}
