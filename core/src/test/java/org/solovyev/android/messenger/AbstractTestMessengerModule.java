package org.solovyev.android.messenger;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.*;
import com.google.inject.util.Modules;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.http.CachingImageLoader;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.DefaultAccountService;
import org.solovyev.android.messenger.accounts.SqliteAccountDao;
import org.solovyev.android.messenger.accounts.connection.AccountConnections;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.accounts.connection.DefaultAccountConnections;
import org.solovyev.android.messenger.accounts.connection.DefaultAccountConnectionsService;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.DefaultChatMessageService;
import org.solovyev.android.messenger.messages.SqliteChatMessageDao;
import org.solovyev.android.messenger.notifications.DefaultNotificationService;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.DefaultRealmService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.android.network.NetworkStateServiceImpl;
import org.solovyev.tasks.TaskService;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;
import static org.solovyev.tasks.Tasks.newTaskService;

public abstract class AbstractTestMessengerModule extends AbstractModule {

	private final Map<Class<?>, Object> bindings = new HashMap<Class<?>, Object>();

	@Nonnull
	private final Application application;

	public AbstractTestMessengerModule(@Nonnull Application application) {
		this.application = application;
	}

	@Override
	protected void configure() {
		bind(Executor.class).toInstance(new Executor() {
			@Override
			public void execute(Runnable command) {
				command.run();
			}
		});
		bind(SQLiteOpenHelperConfiguration.class).to(TestMessengerDbConfiguration.class);
		bind(SQLiteOpenHelper.class).to(TestSQLiteOpenHelper.class);

		bind(MessengerSecurityService.class).to(TestSecurityService.class);
		bind(TaskService.class).toInstance(newTaskService());

		bind(RealmService.class).to(DefaultRealmService.class);
		bind(AccountConnections.class).to(DefaultAccountConnections.class);
		bind(AccountConnectionsService.class).to(DefaultAccountConnectionsService.class);
		bind(AccountService.class).to(DefaultAccountService.class);
		bind(AccountDao.class).to(SqliteAccountDao.class);

		bind(MessengerListeners.class).to(DefaultMessengerListeners.class);
		bind(NotificationService.class).to(DefaultNotificationService.class);
		bind(ExceptionHandler.class).to(DefaultExceptionHandler.class);
		bind(Configuration.class).toInstance(newAppConfiguration());
		bind(org.solovyev.android.http.ImageLoader.class).to(ImageLoader.class);
		bind(NetworkStateService.class).to(NetworkStateServiceImpl.class).in(Scopes.SINGLETON);

		bind(UserDao.class).to(SqliteUserDao.class);
		bind(UserService.class).to(DefaultUserService.class);

		bind(ChatDao.class).to(SqliteChatDao.class);
		bind(ChatService.class).to(DefaultChatService.class);

		bind(ChatMessageDao.class).to(SqliteChatMessageDao.class);
		bind(ChatMessageService.class).to(DefaultChatMessageService.class);

		bind(SyncService.class).toInstance(mock(SyncService.class));

		bind(Context.class).toInstance(application);

		Set<Map.Entry<Class<?>, Object>> entries = bindings.entrySet();
		for (Map.Entry<Class<?>, Object> entry : entries) {
			bind((Class<Object>) entry.getKey()).toInstance(entry.getValue());
		}
	}

	@Nonnull
	protected abstract Configuration newAppConfiguration();

	public void addBinding(Class<?> type, Object object) {
		bindings.put(type, object);
	}

	public void setUp(@Nonnull Object testObject,
					  @Nonnull AbstractTestMessengerModule module) {
		Module roboGuiceModule = RoboGuice.newDefaultRoboModule(application);
		Module testModule = Modules.override(roboGuiceModule).with(module);
		RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE, testModule);
		RoboInjector injector = RoboGuice.getInjector(application);
		injector.injectMembers(testObject);
	}

	public void tearDown() {
		RoboGuice.util.reset();
	}

	@Singleton
	public static class ImageLoader extends CachingImageLoader {

		@Inject
		public ImageLoader(@Nonnull Application context) {
			super(context, "messenger");
		}
	}
}
