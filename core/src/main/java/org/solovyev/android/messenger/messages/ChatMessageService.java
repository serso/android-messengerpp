package org.solovyev.android.messenger.messages;

import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */

/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface ChatMessageService {

	final String NO_ACCOUNT_MESSAGE_ID = "empty";

	void init();

	@Nonnull
	Entity generateEntity(@Nonnull Account account);

	@Nonnull
	List<ChatMessage> getChatMessages(@Nonnull Entity realmChat);

	void setMessageIcon(@Nonnull ChatMessage message, @Nonnull ImageView imageView);

	@Nullable
	ChatMessage sendChatMessage(@Nonnull Entity user, @Nonnull Chat chat, @Nonnull ChatMessage chatMessage) throws AccountException;

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

	void removeAllMessagesInRealm(@Nonnull String realmId);
}
