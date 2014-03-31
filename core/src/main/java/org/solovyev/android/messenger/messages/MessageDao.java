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

package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface MessageDao extends Dao<Message> {

	@Nullable
	Message read(@Nonnull String messageId);

	@Nonnull
	List<Message> readMessages(@Nonnull String chatId);

	@Nonnull
	MessagesMergeDaoResult mergeMessages(@Nonnull String chatId, @Nonnull Collection<? extends Message> messages);

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

	@Nonnull
	List<AProperty> readPropertiesById(@Nonnull String messageId);

	@Nullable
	Message readSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient);
}
