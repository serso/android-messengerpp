package org.solovyev.android.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
public class MessengerService extends Service {

    @NotNull
    public static final String API_SERVICE = "org.solovyev.android.messenger.API_SERVICE";

    @Nullable
    private RealmConnection realmConnection;

    @NotNull
    private final MessengerApi.Stub remoteMessengerApi = new MessengerApi.Stub() {

        @Override
        public void loginUser(String realm, String login, String password, ServiceCallback callback) throws RemoteException {
            try {
                getServiceLocator().getAuthService().loginUser(realm, login, password, null, MessengerService.this);
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

        //final Timer timer = new Timer("Messenger sync task", true);
        //timer.scheduleAtFixedRate(new SyncTimerTask(this), 10000L, 30L * 1000L);

        this.realmConnection = MessengerConfigurationImpl.getInstance().getRealm().createRealmConnection(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                realmConnection.start();
            }
        }).start();
    }

    @NotNull
    private static ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @Override
    public void onDestroy() {
        try {
            if (this.realmConnection != null) {
                this.realmConnection.stop();
            }
        } finally {
            super.onDestroy();
        }
    }
}
