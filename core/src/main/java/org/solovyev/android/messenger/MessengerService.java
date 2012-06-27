package org.solovyev.android.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.longpoll.ApiLongPollService;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
public class MessengerService extends Service {

    @NotNull
    public static final String API_SERVICE = "org.solovyev.android.messenger.API_SERVICE";

    @NotNull
    private final AtomicBoolean stopPolling = new AtomicBoolean();

    @NotNull
    private final MessengerApi.Stub remoteMessengerApi = new MessengerApi.Stub() {

        @Override
        public void loginUser(String login, String password, ServiceCallback callback) throws RemoteException {
            try {
                getServiceLocator().getAuthServiceFacade().loginUser(MessengerService.this, login, password, null);
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

        stopPolling.set(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // first loop guarantees that if something gone wrong we will initiate new long polling session
                while (!stopPolling.get()) {
                    try {

                        Log.i("LongPolling", "Long polling initiated!");
                        Object longPollingData = getLongPollingApi().startLongPolling();

                        // second loop do long poll job for one session
                        while (!stopPolling.get()) {
                            Log.i("LongPolling", "Long polling started!");

                            final User user = getServiceLocator().getAuthServiceFacade().getUser(MessengerService.this);
                            final LongPollResult longPollResult = getLongPollingApi().waitForResult(longPollingData);
                            if (longPollResult != null) {
                                longPollingData = longPollResult.updateLongPollServerData(longPollingData);
                                longPollResult.doUpdates(user, MessengerService.this);
                            }

                            Log.i("LongPolling", "Long polling ended!");

                        }

                    } catch (RuntimeException e) {
                        Log.e("LongPolling", e.getMessage(), e);
                    } catch (UserIsNotLoggedInException e) {
                        // wait for login
                    }
                }
            }
        }).start();
    }

    @NotNull
    private static ApiLongPollService getLongPollingApi() {
        return getServiceLocator().getApiLongPollService();
    }

    @NotNull
    private static ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPolling.set(true);
    }
}
