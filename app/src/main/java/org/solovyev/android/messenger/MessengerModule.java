package org.solovyev.android.messenger;

import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.AbstractModule;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.db.MessengerSQLiteOpenHelper;
import org.solovyev.android.messenger.http.MessengerCachingImageLoader;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.DefaultChatMessageService;
import org.solovyev.android.messenger.messages.SqliteChatMessageDao;
import org.solovyev.android.messenger.realms.DefaultRealmService;
import org.solovyev.android.messenger.realms.RealmDao;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.SqliteRealmDao;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.security.AuthServiceFacadeImpl;
import org.solovyev.android.messenger.security.AuthServiceImpl;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.realms.vk.registration.DummyRegistrationService;
import org.solovyev.android.network.MessengerNetworkStateService;
import org.solovyev.android.network.NetworkStateService;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 10:27 PM
 */
public class MessengerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SQLiteOpenHelperConfiguration.class).to(MessengerDbConfiguration.class);
        bind(SQLiteOpenHelper.class).to(MessengerSQLiteOpenHelper.class);

        bind(RealmService.class).to(DefaultRealmService.class);
        bind(RealmDao.class).to(SqliteRealmDao.class);

        bind(MessengerConfiguration.class).to(MessengerConfigurationImpl.class);
        bind(AuthService.class).to(AuthServiceImpl.class);
        bind(AuthServiceFacade.class).to(AuthServiceFacadeImpl.class);
        bind(ImageLoader.class).to(MessengerCachingImageLoader.class);
        bind(NetworkStateService.class).to(MessengerNetworkStateService.class);

        bind(UserDao.class).to(SqliteUserDao.class);
        bind(UserService.class).to(DefaultUserService.class);

        bind(ChatDao.class).to(SqliteChatDao.class);
        bind(ChatService.class).to(DefaultChatService.class);

        bind(ChatMessageDao.class).to(SqliteChatMessageDao.class);
        bind(ChatMessageService.class).to(DefaultChatMessageService.class);

        bind(SyncService.class).to(DefaultSyncService.class);
        bind(RegistrationService.class).to(DummyRegistrationService.class);

        bind(MessengerMultiPaneManager.class).to(MessengerMultiPaneManagerImpl.class);
    }
}
