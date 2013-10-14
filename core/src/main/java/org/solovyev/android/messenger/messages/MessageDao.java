package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.MergeDaoResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface MessageDao {

	@Nullable
	Message read(@Nonnull String messageId);

	@Nonnull
	List<Message> readMessages(@Nonnull String chatId);

	@Nonnull
	MergeDaoResult<Message, String> mergeMessages(@Nonnull String chatId, @Nonnull Collection<? extends Message> messages, boolean allowDelete);

	@Nonnull
	List<String> readMessageIds(@Nonnull String chatId);

	@Nonnull
	String getOldestMessageForChat(@Nonnull String chatId);

	@Nullable
	Message readLastMessage(@Nonnull String chatId);

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

	boolean changeReadStatus(@Nonnull String messageId, boolean read);
	boolean changeMessageState(@Nonnull String messageId, @Nonnull MessageState state);

	void deleteAll();
}
