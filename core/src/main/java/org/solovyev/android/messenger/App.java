package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.google.inject.Inject;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.tasks.TaskService;

import android.widget.Toast;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public final class App {

	@Nonnull
	public static final String TAG = "M++";

	@Nonnull
	private static App instance = new App();

	@Nonnull
	private Application application;

	@Inject
	@Nonnull
	private MessageService messageService;

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
	private AccountService accountService;

	@Inject
	@Nonnull
	private RealmService realmService;

	@Inject
	@Nonnull
	private AccountConnectionsService accountConnectionsService;

	@Inject
	@Nonnull
	private UnreadMessagesCounter unreadMessagesCounter;

	@Inject
	@Nonnull
	private UnreadMessagesNotifier unreadMessagesNotifier;

	@Inject
	@Nonnull
	private ExceptionHandler exceptionHandler;

	@Inject
	@Nonnull
	private NotificationService notificationService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private TaskService taskService;

	@Inject
	@Nonnull
	private MessengerSecurityService securityService;

	@Nonnull
	private Handler uiHandler;

	private void init0(@Nonnull Application application) {
		this.application = application;
		this.uiHandler = Threads.newUiHandler();

		RoboGuice.getBaseApplicationInjector(application).injectMembers(this);

		// init services
		realmService.init();
		accountService.init();
		userService.init();
		chatService.init();
		messageService.init();
		syncService.init();
		unreadMessagesCounter.init();

		// load persistence data
		accountService.load();


		// must be done after all loadings
		accountConnectionsService.init();

		networkStateService.startListening(application);
	}

	/*
	**********************************************************************
	*
	*                           STATIC
	*
	**********************************************************************
	*/

	@Nonnull
	public static String newTag(@Nonnull String tag) {
		return newSubTag(TAG, tag);
	}

	@Nonnull
	public static String newSubTag(@Nonnull String tag, @Nonnull String subTag) {
		return tag + "/" + subTag;
	}

	public static void init(@Nonnull Application application) {
		instance.init0(application);
	}

	public static void showToast(int textResId) {
		Toast.makeText(getApplication(), textResId, Toast.LENGTH_SHORT).show();
	}

	@Nonnull
	public static MessageService getMessageService() {
		return instance.messageService;
	}

	@Nonnull
	public static UserService getUserService() {
		return instance.userService;
	}

	@Nonnull
	public static ChatService getChatService() {
		return instance.chatService;
	}

	@Nonnull
	public static SyncService getSyncService() {
		return instance.syncService;
	}

	@Nonnull
	public static AccountService getAccountService() {
		return instance.accountService;
	}

	@Nonnull
	public static RealmService getRealmService() {
		return instance.realmService;
	}

	@Nonnull
	public static AccountConnectionsService getAccountConnectionsService() {
		return instance.accountConnectionsService;
	}

	@Nonnull
	public static UnreadMessagesCounter getUnreadMessagesCounter() {
		return instance.unreadMessagesCounter;
	}

	@Nonnull
	public static UnreadMessagesNotifier getUnreadMessagesNotifier() {
		return instance.unreadMessagesNotifier;
	}

	@Nonnull
	public static ExceptionHandler getExceptionHandler() {
		return instance.exceptionHandler;
	}

	@Nonnull
	public static NotificationService getNotificationService() {
		return instance.notificationService;
	}

	@Nonnull
	public static NetworkStateService getNetworkStateService() {
		return instance.networkStateService;
	}

	@Nonnull
	public static TaskService getTaskService() {
		return instance.taskService;
	}

	@Nonnull
	public static MessengerSecurityService getSecurityService() {
		return instance.securityService;
	}

	@Nonnull
	public static Application getApplication() {
		return instance.application;
	}

	public static void exit(Activity activity) {
		MessengerApplication.exit(instance.application, activity);
	}

	@Nonnull
	public static EventManager getEventManager(@Nonnull Context context) {
		return RoboGuice.getInjector(context).getInstance(EventManager.class);
	}

	@Nonnull
	public static SharedPreferences getPreferences() {
		return getDefaultSharedPreferences(getApplication());
	}

	@Nonnull
	public static Handler getUiHandler() {
		return instance.uiHandler;
	}

	@Nonnull
	public static Class<? extends Activity> getMainActivityClass() {
		return MainActivity.class;
	}

	/*
	**********************************************************************
	*
	*                           FOR TESTS ONLY
	*
	**********************************************************************
	*/

	@Nonnull
	static App getInstance() {
		return instance;
	}

	public void setApplication(@Nonnull Application application) {
		this.application = application;
	}

	public void setMessageService(@Nonnull MessageService messageService) {
		this.messageService = messageService;
	}

	public void setUserService(@Nonnull UserService userService) {
		this.userService = userService;
	}

	public void setChatService(@Nonnull ChatService chatService) {
		this.chatService = chatService;
	}

	public void setSyncService(@Nonnull SyncService syncService) {
		this.syncService = syncService;
	}

	public void setAccountService(@Nonnull AccountService accountService) {
		this.accountService = accountService;
	}

	public void setRealmService(@Nonnull RealmService realmService) {
		this.realmService = realmService;
	}

	public void setAccountConnectionsService(@Nonnull AccountConnectionsService accountConnectionsService) {
		this.accountConnectionsService = accountConnectionsService;
	}

	public void setUnreadMessagesCounter(@Nonnull UnreadMessagesCounter unreadMessagesCounter) {
		this.unreadMessagesCounter = unreadMessagesCounter;
	}

	public void setUnreadMessagesNotifier(@Nonnull UnreadMessagesNotifier unreadMessagesNotifier) {
		this.unreadMessagesNotifier = unreadMessagesNotifier;
	}

	public void setExceptionHandler(@Nonnull ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void setNotificationService(@Nonnull NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void setNetworkStateService(@Nonnull NetworkStateService networkStateService) {
		this.networkStateService = networkStateService;
	}

	public void setTaskService(@Nonnull TaskService taskService) {
		this.taskService = taskService;
	}

	public void setSecurityService(@Nonnull MessengerSecurityService securityService) {
		this.securityService = securityService;
	}

	public static void startBackgroundService() {
		MessengerApplication.startBackgroundService(instance.application);
	}
}
