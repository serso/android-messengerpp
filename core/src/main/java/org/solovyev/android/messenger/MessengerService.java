package org.solovyev.android.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.service.RoboService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
public class MessengerService extends RoboService implements NetworkStateListener {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private NetworkStateService networkStateService;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @Nonnull
    private RealmConnections realmConnections;

    @Nullable
    private RealmEventListener realmEventListener;

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        realmConnections = new RealmConnections(this);

        networkStateService.addListener(this);

        realmEventListener = new RealmEventListener();
        realmService.addListener(realmEventListener);

        tryStartConnectionsFor(realmService.getRealms());
    }

    private void tryStartConnectionsFor(@Nonnull Collection<Realm> realms) {
        final boolean start = canStartConnection();
        realmConnections.startConnectionsFor(realms, start);
    }

    private boolean canStartConnection() {
        final NetworkData networkData = networkStateService.getNetworkData();
        return networkData.getState() == NetworkState.CONNECTED;
    }

    @Override
    public void onDestroy() {
        try {
            networkStateService.removeListener(this);

            if (realmEventListener != null) {
                realmService.removeListener(realmEventListener);
            }

            realmConnections.tryStopAll();
        } finally {
            super.onDestroy();
        }
    }

    @Override
    public void onNetworkEvent(@Nonnull NetworkData networkData) {
        switch (networkData.getState()) {
            case UNKNOWN:
                break;
            case CONNECTED:
                realmConnections.tryStartAll();
                break;
            case NOT_CONNECTED:
                realmConnections.tryStopAll();
                break;
        }
    }

    private final class RealmEventListener extends AbstractJEventListener<RealmEvent> implements JEventListener<RealmEvent> {

        private RealmEventListener() {
            super(RealmEvent.class);
        }

        @Override
        public void onEvent(@Nonnull RealmEvent event) {
            final Realm realm = event.getRealm();
            if (event instanceof RealmAddedEvent) {
                tryStartConnectionsFor(Arrays.asList(realm));
            } else if (event instanceof RealmRemovedEvent) {
                realmConnections.removeConnectionFor(realm);
            } else if (event instanceof RealmChangedEvent) {
                realmConnections.updateRealm(realm, canStartConnection());
            }
        }
    }

    @ThreadSafe
    private static final class RealmConnections {

        @Nonnull
        private final Context context;

        @GuardedBy("realmConnections")
        @Nonnull
        private final Set<RealmConnection> realmConnections = new HashSet<RealmConnection>();

        private RealmConnections(@Nonnull Context context) {
            this.context = context.getApplicationContext();
        }

        public void startConnectionsFor(@Nonnull Collection<Realm> realms, boolean start) {
            synchronized (realmConnections) {
                for (final Realm realm : realms) {
                    // are there any realm connections for current realm?
                    boolean contains = Iterables.any(realmConnections, new RealmConnectionFinder(realm));

                    if (!contains) {
                        // there is no realm connection for current realm => need to add
                        final RealmConnection realmConnection = realm.newRealmConnection(context);

                        realmConnections.add(realmConnection);

                        if (start) {
                            startRealmConnection(realmConnection);
                        }
                    }
                }
            }
        }

        private static void startRealmConnection(@Nonnull final RealmConnection realmConnection) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (realmConnection.isStopped()) {
                        realmConnection.start();
                    }
                }
            }, "Realm connection thread: " + realmConnection.getRealm().getId()).start();
        }

        public void tryStopAll() {
            synchronized (this.realmConnections) {
                for (RealmConnection realmConnection : realmConnections) {
                    if (!realmConnection.isStopped()) {
                        realmConnection.stop();
                    }
                }
            }
        }

        public void tryStartAll() {
            synchronized (this.realmConnections) {
                for (RealmConnection realmConnection : realmConnections) {
                    if (realmConnection.isStopped()) {
                        startRealmConnection(realmConnection);
                    }
                }
            }
        }

        public void removeConnectionFor(@Nonnull Realm realm) {
            synchronized (this.realmConnections) {
                // remove realm connections belonged to specified realm
                final List<RealmConnection> removedConnections = new ArrayList<RealmConnection>();
                Iterables.removeIf(this.realmConnections, PredicateSpy.spyOn(new RealmConnectionFinder(realm), removedConnections));

                // stop them
                for (RealmConnection removedConnection : removedConnections) {
                    if (!removedConnection.isStopped()) {
                        removedConnection.stop();
                    }
                }
            }
        }

        public void updateRealm(@Nonnull Realm realm, boolean start) {
            synchronized (this.realmConnections) {
                removeConnectionFor(realm);
                startConnectionsFor(Arrays.asList(realm), start);
            }
        }

        private static class RealmConnectionFinder implements Predicate<RealmConnection> {

            @Nonnull
            private final Realm realm;

            public RealmConnectionFinder(@Nonnull Realm realm) {
                this.realm = realm;
            }

            @Override
            public boolean apply(@Nullable RealmConnection realmConnection) {
                return realmConnection != null && realmConnection.getRealm().equals(realm);
            }
        }

        private static final class PredicateSpy<T> implements Predicate<T> {

            @Nonnull
            private final Predicate<T> predicate;

            @Nonnull
            private final Collection<T> spyResult;

            private PredicateSpy(@Nonnull Predicate<T> predicate, @Nonnull Collection<T> spyResult) {
                this.predicate = predicate;
                this.spyResult = spyResult;
            }

            @Nonnull
            private static <T> PredicateSpy<T> spyOn(@Nonnull Predicate<T> predicate, @Nonnull Collection<T> spyResult) {
                return new PredicateSpy<T>(predicate, spyResult);
            }

            @Override
            public boolean apply(@Nullable T input) {
                boolean applied = predicate.apply(input);
                if ( applied ) {
                    spyResult.add(input);
                }
                return applied;
            }
        }
    }
}
