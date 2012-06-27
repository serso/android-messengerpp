package org.solovyev.android.messenger;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTimeZone;
import org.solovyev.android.date.FastDateTimeZoneProvider;
import org.solovyev.android.db.CommonSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.messenger.security.ApiAuthenticator;
import org.solovyev.android.messenger.users.DummyMessengerApi;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */
public abstract class MessengerApplication extends Application implements MessengerApiProvider {

    private MessengerApiConnection connection;

    public static class Preferences {

        public static class Gui {
            public static class Chat {
                public static Preference<Boolean> showUserIcon = new BooleanPreference("gui.chat.showUserIcon", true);
                public static Preference<Boolean> showFriendIconInChat = new BooleanPreference("gui.chat.showFriendIconInChat", true);
                public static Preference<Boolean> showFriendIconInPrivateChat = new BooleanPreference("gui.chat.showFriendIconInPrivateChat", true);
                public static Preference<UserIconPosition> userIconPosition = StringPreference.newInstance("gui.chat.userIconPosition", UserIconPosition.right, UserIconPosition.class);

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

        MessengerConfigurationImpl.getInstance().setRealm(getRealm());
        final CommonSQLiteOpenHelper sqliteOpenHelper = new CommonSQLiteOpenHelper(this, getSqliteOpenHelperConfiguration());

        MessengerConfigurationImpl.getInstance().setSqliteOpenHelper(sqliteOpenHelper);

        MessengerConfigurationImpl.getInstance().setServiceLocator(getServiceLocator());
        MessengerConfigurationImpl.getInstance().setDaoLocator(getDaoLocator());
        MessengerConfigurationImpl.getInstance().setAuthenticator(getAuthenticator());

        final Intent intent = new Intent();
        intent.setClass(this, MessengerService.class);
        startService(intent);
        bindService();

        MessengerConfigurationImpl.getInstance().setMessengerApiProvider(this);

        // load persistence data
        MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthService().load(this);
    }

    @NotNull
    protected abstract SQLiteOpenHelperConfiguration getSqliteOpenHelperConfiguration();

    @NotNull
    protected abstract DaoLocator getDaoLocator();

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

    @NotNull
    protected abstract String getRealm();

    @NotNull
    protected abstract ApiAuthenticator getAuthenticator();

    @NotNull
    protected abstract ServiceLocator getServiceLocator();

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
