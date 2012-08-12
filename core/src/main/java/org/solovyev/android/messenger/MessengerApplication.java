package org.solovyev.android.messenger;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DummyMessengerApi;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateController;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */
public abstract class MessengerApplication extends Application implements MessengerApiProvider, MessengerServiceLocator {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private AuthService authService;

    @Inject
    @NotNull
    private AuthServiceFacade authServiceFacade;

    @Inject
    @NotNull
    private ChatMessageService chatMessageService;

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private SyncService syncService;

    @Inject
    @NotNull
    private RealmService realmService;

    @Override
    @NotNull
    public ChatMessageService getChatMessageService() {
        return chatMessageService;
    }

    @Override
    @NotNull
    public UserService getUserService() {
        return userService;
    }

    @Override
    @NotNull
    public ChatService getChatService() {
        return chatService;
    }

    @Override
    @NotNull
    public AuthService getAuthService() {
        return authService;
    }

    @Override
    @NotNull
    public SyncService getSyncService() {
        return syncService;
    }

    @Override
    @NotNull
    public RealmService getRealmService() {
        return realmService;
    }

    @Override
    @NotNull
    public AuthServiceFacade getAuthServiceFacade() {
        return authServiceFacade;
    }


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    private MessengerApiConnection connection;

    @NotNull
    private static MessengerApplication instance;

    public MessengerApplication() {
        instance = this;
    }

    @NotNull
    public static MessengerServiceLocator getServiceLocator() {
        return instance;
    }

    public static class Preferences {

        public static class Gui {
            public static class Chat {
                public static Preference<Boolean> showUserIcon = new BooleanPreference("gui.chat.showUserIcon", true);
                public static Preference<Boolean> showContactIconInChat = new BooleanPreference("gui.chat.showContactIconInChat", true);
                public static Preference<Boolean> showContactIconInPrivateChat = new BooleanPreference("gui.chat.showContactIconInPrivateChat", true);
                public static Preference<UserIconPosition> userMessagesPosition = StringPreference.newInstance("gui.chat.userMessagesPosition", UserIconPosition.left, UserIconPosition.class);

                public static enum UserIconPosition {
                    left,
                    right
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Joda time for android
        System.setProperty("org.joda.time.DateTimeZone.Provider", FastDateTimeZoneProvider.class.getName());

        DateTimeZone.setDefault(DateTimeZone.UTC);

        RoboGuice.getBaseApplicationInjector(this).injectMembers(this);


        // load persistence data
        this.authService.load(this);

        // init services
        this.userService.init();
        this.chatService.init();

        NetworkStateController.getInstance().startListening(this);

        final Intent intent = new Intent();
        intent.setClass(this, MessengerService.class);
        startService(intent);
        bindService();
    }

    private void bindService() {
        if (connection == null) {
            connection = new MessengerApiConnection();

            bindService(new Intent(MessengerService.API_SERVICE), connection, Context.BIND_AUTO_CREATE);

            Log.d(getClass().getSimpleName(), "bindService()");
        } else {
            Toast.makeText(MessengerApplication.this, "Cannot bind - service already bound", Toast.LENGTH_SHORT).show();
        }
    }

    @NotNull
    @Override
    public MessengerApi getMessengerApi() {
        return connection.getMessengerApi();
    }

    private static final class MessengerApiConnection implements ServiceConnection, MessengerApiProvider {

        @Nullable
        private MessengerApi messengerApi;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messengerApi = MessengerApi.Stub.asInterface(service);
            Log.d(getClass().getSimpleName(), "onServiceConnected()");
        }

        public void onServiceDisconnected(ComponentName className) {
            messengerApi = null;
            Log.d(getClass().getSimpleName(), "onServiceDisconnected");
        }

        @NotNull
        @Override
        public MessengerApi getMessengerApi() {
            MessengerApi localMessengerApi = messengerApi;
            if (localMessengerApi == null) {
                localMessengerApi = new DummyMessengerApi();
            }
            return localMessengerApi;
        }
    }
}
