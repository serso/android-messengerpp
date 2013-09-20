package org.solovyev.android.messenger;

import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

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
import org.solovyev.android.messenger.notifications.DefaultNotificationService;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.messenger.realms.vk.registration.DummyRegistrationService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.android.network.NetworkStateServiceImpl;
import org.solovyev.tasks.TaskService;
import org.solovyev.tasks.Tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 10:27 PM
 */
public class MessengerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ExecutorService.class).toInstance(Executors.newSingleThreadExecutor());
		bind(TaskService.class).toInstance(Tasks.newTaskService());

		bind(MessengerListeners.class).to(DefaultMessengerListeners.class);
		bind(MessengerExceptionHandler.class).to(DefaultMessengerExceptionHandler.class);
		bind(NotificationService.class).to(DefaultNotificationService.class);
		bind(SQLiteOpenHelperConfiguration.class).to(MessengerDbConfiguration.class);
		bind(SQLiteOpenHelper.class).to(MessengerSQLiteOpenHelper.class);

		bind(AccountService.class).to(DefaultAccountService.class);
		bind(RealmDao.class).to(SqliteRealmDao.class);

		bind(RealmConnectionsService.class).to(RealmConnectionsServiceImpl.class);
		bind(MessengerConfiguration.class).to(MessengerConfigurationImpl.class);
		bind(ImageLoader.class).to(MessengerCachingImageLoader.class);
		bind(NetworkStateService.class).to(NetworkStateServiceImpl.class).in(Scopes.SINGLETON);

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
