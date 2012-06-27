package org.solovyev.android.messenger.vk;

import android.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.security.ApiAuthenticator;
import org.solovyev.android.messenger.vk.chats.VkApiChatService;
import org.solovyev.android.messenger.vk.longpoll.VkApiLongPollService;
import org.solovyev.android.messenger.vk.registration.DummyRegistrationService;
import org.solovyev.android.messenger.vk.secutiry.VkApiAuthenticator;
import org.solovyev.android.messenger.vk.users.VkApiUserService;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:48 PM
 */
public class VkMessengerApplication extends MessengerApplication {

    @NotNull
    public static final String CLIENT_ID = "2970921";

    @NotNull
    public static final String CLIENT_SECRET = "Scm7M1vxOdDjpeVj81jw";

    @NotNull
    public static final String DB_NAME = "vk";
    public static final int DB_VERSION = 1;


    @Override
    public void onCreate() {
        super.onCreate();

        VkConfigurationImpl.getInstance().setClientId(CLIENT_ID);
        VkConfigurationImpl.getInstance().setClientSecret(CLIENT_SECRET);
    }

    @NotNull
    @Override
    protected SQLiteOpenHelperConfiguration getSqliteOpenHelperConfiguration() {
        return new SQLiteOpenHelperConfiguration() {
            @NotNull
            @Override
            public String getName() {
                return DB_NAME;
            }

            @Override
            public SQLiteDatabase.CursorFactory getCursorFactory() {
                return null;
            }

            @Override
            public int getVersion() {
                return DB_VERSION;
            }
        };
    }

    @NotNull
    @Override
    protected DaoLocator getDaoLocator() {
        return new DefaultDaoLocator(MessengerConfigurationImpl.getInstance().getSqliteOpenHelper());
    }

    @NotNull
    @Override
    protected String getRealm() {
        return "vk";
    }

    @NotNull
    @Override
    protected ApiAuthenticator getAuthenticator() {
        return new VkApiAuthenticator();
    }

    @NotNull
    @Override
    protected ServiceLocator getServiceLocator() {
        return new DefaultServiceLocator(this, new VkApiUserService(), new VkApiChatService(), new VkApiLongPollService(), new DummyRegistrationService());
    }

}

