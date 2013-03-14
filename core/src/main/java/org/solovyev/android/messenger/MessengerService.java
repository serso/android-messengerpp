package org.solovyev.android.messenger;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEvent;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.service.RoboService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
public class MessengerService extends RoboService implements NetworkStateListener {

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static final int NOTIFICATION_ID = 10002029;

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

        final Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.drawable.mpp_app_icon);
        notificationBuilder.setContentText(getString(R.string.mpp_app_name));
        startForeground(NOTIFICATION_ID, notificationBuilder.getNotification());

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
            switch (event.getType()) {
                case created:
                    tryStartConnectionsFor(Arrays.asList(realm));
                    break;
                case changed:
                    realmConnections.removeConnectionFor(realm);
                    break;
                case removed:
                    realmConnections.updateRealm(realm, canStartConnection());
                    break;
            }
        }
    }

}
