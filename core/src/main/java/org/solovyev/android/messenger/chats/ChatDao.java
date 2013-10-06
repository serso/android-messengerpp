package org.solovyev.android.messenger.chats;

import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatDao extends Dao<Chat> {

	@Nonnull
	MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats);

	@Nonnull
	List<String> loadUserChatIds(@Nonnull String userId);

	@Nonnull
	Collection<String> readAllIds();

	@Nonnull
	List<AProperty> loadChatPropertiesById(@Nonnull String chatId);

	@Nonnull
	List<Chat> loadUserChats(@Nonnull String userId);

	@Nonnull
	List<User> loadChatParticipants(@Nonnull String chatId);

	@Nullable
	Chat read(@Nonnull String chatId);

	/**
	 * Method updates chat in the storage
	 *
	 * @param chat chat to be updated
	 * @return number of updated rows
	 */
	long update(@Nonnull Chat chat);

	void deleteAll();

	void deleteAllChatsForAccount(@Nonnull String accountId);

	/**
	 * Key: chat for which unread messages exist, value: number of unread messages
	 *
	 * @return map of chats with unread messages counts for them
	 */
	@Nonnull
	Map<Entity, Integer> getUnreadChats();

	void delete(@Nonnull User user, @Nonnull Chat chat);
}
