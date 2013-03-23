package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import org.joda.time.DateTimeZone;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */
public class MessengerApplication extends Application implements MessengerServiceLocator, MessengerExceptionHandler {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    private static MessengerApplication instance;

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private AuthService authService;

    @Inject
    @Nonnull
    private AuthServiceFacade authServiceFacade;

    @Inject
    @Nonnull
    private ChatMessageService chatMessageService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private SyncService syncService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private UnreadMessagesCounter unreadMessagesCounter;

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
    private final Handler uiHandler = new Handler();

    public MessengerApplication() {
        instance = this;
    }

    @Nonnull
    public static MessengerServiceLocator getServiceLocator() {
        return instance;
    }

    @Nonnull
    public static MessengerApplication getApp() {
        return instance;
    }

    @Override
    public void handleException(@Nonnull final Throwable e) {
        if (e instanceof HttpRuntimeIoException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MessengerApplication.this, "No internet connection available: connect to the network and try again!", Toast.LENGTH_LONG).show();
                }
            });
            Log.d("Msg_NoInternet", e.getMessage(), e);
        } else if (e instanceof IllegalJsonRuntimeException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MessengerApplication.this, "The response from server is not valid!", Toast.LENGTH_LONG).show();
                }
            });
            Log.e("Msg_InvalidJson", e.getMessage(), e);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MessengerApplication.this, "Something is going wrong!", Toast.LENGTH_LONG).show();
                }
            });
            Log.e("Msg_Exception", e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public ChatMessageService getChatMessageService() {
        return chatMessageService;
    }

    @Override
    @Nonnull
    public UserService getUserService() {
        return userService;
    }

    @Override
    @Nonnull
    public ChatService getChatService() {
        return chatService;
    }

    @Override
    @Nonnull
    public AuthService getAuthService() {
        return authService;
    }

    @Override
    @Nonnull
    public SyncService getSyncService() {
        return syncService;
    }

    @Override
    @Nonnull
    public RealmService getRealmService() {
        return realmService;
    }

    @Override
    @Nonnull
    public AuthServiceFacade getAuthServiceFacade() {
        return authServiceFacade;
    }

    @Override
    @Nonnull
    public NetworkStateService getNetworkStateService() {
        return networkStateService;
    }

    @Nonnull
    @Override
    public MessengerExceptionHandler getExceptionHandler() {
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Joda time for android
        System.setProperty("org.joda.time.DateTimeZone.Provider", FastDateTimeZoneProvider.class.getName());

        DateTimeZone.setDefault(DateTimeZone.UTC);

        MessengerPreferences.setDefaultValues(this);

        RoboGuice.getBaseApplicationInjector(this).injectMembers(this);

        // init services
        this.realmService.init();
        this.userService.init();
        this.chatService.init();
        this.chatMessageService.init();
        this.syncService.init();
        this.unreadMessagesCounter.init();

        // load persistence data
        this.realmService.load();
        this.authService.load();

        this.networkStateService.startListening(this);
    }

    public void exit(@Nonnull Activity activity) {
        realmService.stopAllRealmConnections();

        final Intent serviceIntent = new Intent();
        serviceIntent.setClass(this, MessengerService.class);
        stopService(serviceIntent);

        activity.finish();
    }
}
