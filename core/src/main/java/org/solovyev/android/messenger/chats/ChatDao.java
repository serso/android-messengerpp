/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.chats;

import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ChatDao extends LinkedEntitiesDao<Chat>, Dao<Chat> {

	@Nonnull
	ChatMergeDaoResult mergeChats(@Nonnull String userId, @Nonnull Iterable<? extends AccountChat> chats);

	@Nonnull
	Collection<String> readAllIds();

	@Nonnull
	List<AProperty> readPropertiesById(@Nonnull String chatId);

	@Nonnull
	List<Chat> readChatsByUserId(@Nonnull String userId);

	@Nonnull
	Collection<String> readLinkedEntityIds(@Nonnull String userId);

	@Nonnull
	List<User> readParticipants(@Nonnull String chatId);

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

	/**
	 * Key: chat for which unread messages exist, value: number of unread messages
	 *
	 * @return map of chats with unread messages counts for them
	 */
	@Nonnull
	Map<Entity, Integer> getUnreadChats();

	void delete(@Nonnull User user, @Nonnull Chat chat);

	@Nonnull
	List<String> readLastChatIds(@Nullable String userId, boolean privateChat, int count);
}
