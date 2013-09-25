package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.tasks.TaskService;

import com.google.inject.Inject;

public final class App {

	@Nonnull
	public static final String TAG = "M++";

	private static final App instance = new App();

	@Nonnull
	private MessengerApplication application;

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
	private MessengerExceptionHandler exceptionHandler;

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

	private void init0(@Nonnull MessengerApplication application) {
		this.application = application;

		RoboGuice.getBaseApplicationInjector(application).injectMembers(this);

		// init services
		realmService.init();
		accountService.init();
		userService.init();
		chatService.init();
		chatMessageService.init();
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

	public static void init(@Nonnull MessengerApplication application) {
		instance.init0(application);
	}

	@Nonnull
	public static ChatMessageService getChatMessageService() {
		return instance.chatMessageService;
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
	public static MessengerExceptionHandler getExceptionHandler() {
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
		instance.application.exit(activity);
	}
}
