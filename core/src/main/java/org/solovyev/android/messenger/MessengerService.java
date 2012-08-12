package org.solovyev.android.messenger;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateController;
import org.solovyev.android.network.NetworkStateListener;
import roboguice.service.RoboService;

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
    private Realm realm;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @NotNull
    public static final String API_SERVICE = "org.solovyev.android.messenger.API_SERVICE";

    @Nullable
    private RealmConnection realmConnection;

    @NotNull
    private final Object realmConnectionLock = new Object();

    @NotNull
    private final MessengerApi.Stub remoteMessengerApi = new MessengerApi.Stub() {

        @Inject
        @NotNull
        public AuthService authService;

        @Override
        public void loginUser(String realm, String login, String password, ServiceCallback callback) throws RemoteException {
            try {
                this.authService.loginUser(realm, login, password, null, MessengerService.this);
                callback.onSuccess();
            } catch (InvalidCredentialsException e) {
                callback.onFailure(e.getMessage());
            } catch (ApiResponseErrorException e) {
                callback.onApiError(e.getApiError());
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return remoteMessengerApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NetworkStateController.getInstance().add(this);
        final NetworkData networkData = NetworkStateController.getInstance().getNetworkData();

        //final Timer timer = new Timer("Messenger sync task", true);
        //timer.scheduleAtFixedRate(new SyncTimerTask(this), 10000L, 30L * 1000L);

        synchronized (realmConnectionLock) {
            this.realmConnection = this.realm.createRealmConnection(this);

            if (networkData.getState() == NetworkState.CONNECTED) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (realmConnectionLock) {
                            realmConnection.start();
                        }
                    }
                }).start();

            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            NetworkStateController.getInstance().remove(this);

            synchronized (this.realmConnectionLock) {
                if (this.realmConnection != null && !this.realmConnection.isStopped()) {
                    this.realmConnection.stop();
                }
            }
        } finally {
            super.onDestroy();
        }
    }

    @Override
    public void onNetworkEvent(@NotNull NetworkData networkData) {
        synchronized (this.realmConnectionLock) {
            if (this.realmConnection != null) {
                switch (networkData.getState()) {
                    case UNKNOWN:
                        break;
                    case CONNECTED:
                        if (this.realmConnection.isStopped()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    realmConnection.start();
                                }
                            }).start();
                        }
                        break;
                    case NOT_CONNECTED:
                        if (!this.realmConnection.isStopped()) {
                            realmConnection.stop();
                        }
                        break;
                }
            }
        }
    }
}
