package org.solovyev.android.messenger;

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import roboguice.service.RoboService;

import java.util.ArrayList;
import java.util.List;

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
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private NetworkStateService networkStateService;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @NotNull
    private final List<RealmConnection> realmConnections = new ArrayList<RealmConnection>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        networkStateService.addListener(this);
        final NetworkData networkData = networkStateService.getNetworkData();

        //final Timer timer = new Timer("Messenger sync task", true);
        //timer.scheduleAtFixedRate(new SyncTimerTask(this), 10000L, 30L * 1000L);

        synchronized (realmConnections) {
            for (Realm realm : realmService.getRealms()) {
                final RealmConnection realmConnection = realm.newRealmConnection(this);

                realmConnections.add(realmConnection);

                if (networkData.getState() == NetworkState.CONNECTED) {
                    startRealmConnection(realmConnection);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            networkStateService.removeListener(this);

            synchronized (this.realmConnections) {
                for (RealmConnection realmConnection : realmConnections) {
                    synchronized (realmConnection) {
                        if (!realmConnection.isStopped()) {
                            realmConnection.stop();
                        }
                    }
                }
            }
        } finally {
            super.onDestroy();
        }
    }

    @Override
    public void onNetworkEvent(@NotNull NetworkData networkData) {
        synchronized (this.realmConnections) {
            for (final RealmConnection realmConnection : realmConnections) {
                synchronized (realmConnection) {
                    switch (networkData.getState()) {
                        case UNKNOWN:
                            break;
                        case CONNECTED:
                            if (realmConnection.isStopped()) {
                                startRealmConnection(realmConnection);
                            }
                            break;
                        case NOT_CONNECTED:
                            if (!realmConnection.isStopped()) {
                                realmConnection.stop();
                            }
                            break;
                    }
                }
            }
        }
    }

    private void startRealmConnection(@NotNull final RealmConnection realmConnection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (realmConnection) {
                    realmConnection.start();
                }
            }
        }).start();
    }
}
