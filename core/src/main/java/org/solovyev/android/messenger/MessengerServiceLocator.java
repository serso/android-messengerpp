package org.solovyev.android.messenger;

import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.tasks.TaskService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 1:40 AM
 */
public interface MessengerServiceLocator {

	@Nonnull
	ChatMessageService getChatMessageService();

	@Nonnull
	UserService getUserService();

	@Nonnull
	ChatService getChatService();

	@Nonnull
	SyncService getSyncService();

	@Nonnull
	AccountService getAccountService();

	@Nonnull
	RealmService getRealmService();

	@Nonnull
	NetworkStateService getNetworkStateService();

	@Nonnull
	MessengerExceptionHandler getExceptionHandler();

	@Nonnull
	UnreadMessagesCounter getUnreadMessagesCounter();

	@Nonnull
	NotificationService getNotificationService();

	@Nonnull
	TaskService getTaskService();

	@Nonnull
	MessengerSecurityService getSecurityService();
}
