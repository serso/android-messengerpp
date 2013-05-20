package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.chats.ChatMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatMessageDao {

	@Nonnull
	List<ChatMessage> loadChatMessages(@Nonnull String chatId);

	@Nonnull
	MergeDaoResult<ChatMessage, String> mergeChatMessages(@Nonnull String chatId, @Nonnull Collection<? extends ChatMessage> messages, boolean allowDelete);

	@Nonnull
	List<String> loadChatMessageIds(@Nonnull String chatId);

	@Nonnull
	String getOldestMessageForChat(@Nonnull String chatId);

	@Nullable
	ChatMessage loadLastChatMessage(@Nonnull String chatId);

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

	boolean changeReadStatus(@Nonnull String messageId, boolean read);

	void deleteAllMessages();

	void deleteAllMessagesInRealm(@Nonnull String realmId);
}
