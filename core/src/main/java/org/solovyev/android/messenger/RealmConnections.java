package org.solovyev.android.messenger;

import android.content.Context;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
* User: serso
* Date: 3/6/13
* Time: 10:47 PM
*/
@ThreadSafe
final class RealmConnections {

    @Nonnull
    private final Context context;

    @GuardedBy("realmConnections")
    @Nonnull
    private final Set<RealmConnection> realmConnections = new HashSet<RealmConnection>();

    RealmConnections(@Nonnull Context context) {
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

    public void tryStopFor(@Nonnull Realm realm) {
        synchronized (this.realmConnections) {
            for (RealmConnection realmConnection : realmConnections) {
                if (realm.equals(realmConnection.getRealm()) && !realmConnection.isStopped()) {
                    realmConnection.stop();
                }
            }
        }
    }

    public void tryStartFor(@Nonnull Realm realm) {
        synchronized (this.realmConnections) {
            for (RealmConnection realmConnection : realmConnections) {
                if (realm.equals(realmConnection.getRealm()) && realmConnection.isStopped()) {
                    realmConnection.start();
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

}
