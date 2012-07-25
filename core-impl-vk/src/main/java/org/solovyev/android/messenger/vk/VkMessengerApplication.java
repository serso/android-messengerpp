package org.solovyev.android.messenger.vk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.vk.longpoll.VkRealmLongPollService;
import org.solovyev.android.messenger.vk.registration.DummyRegistrationService;
import org.solovyev.android.messenger.vk.secutiry.VkRealmAuthService;
import org.solovyev.android.messenger.vk.users.VkRealmUserService;

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

    @NotNull
    private static final String REALM_ID = "vk";


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
    protected String getRealmId() {
        return REALM_ID;
    }

    @NotNull
    @Override
    protected DaoLocator getDaoLocator() {
        return new DefaultDaoLocator(MessengerConfigurationImpl.getInstance().getSqliteOpenHelper());
    }

    @NotNull
    @Override
    protected ServiceLocator getServiceLocator() {
        return new DefaultServiceLocator(this, new DummyRegistrationService(), new VkRealm());
    }

    private static class VkRealm extends AbstractRealm {

        protected VkRealm() {
            super(REALM_ID, new VkRealmUserService(), new VkRealmChatService(), new VkRealmAuthService());
        }

        @NotNull
        @Override
        public RealmConnection createRealmConnection(@NotNull Context context) {
            return new LongPollRealmConnection(this, context, new VkRealmLongPollService());
        }
    }

}

