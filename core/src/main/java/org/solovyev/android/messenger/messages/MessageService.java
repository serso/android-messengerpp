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

import android.widget.ImageView;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface MessageService {

	void init();

	@Nonnull
	List<Message> getMessages(@Nonnull Entity chat);

	@Nullable
	Message getSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient);

	@Nullable
	Message getMessage(@Nonnull String messageId);

	void setMessageIcon(@Nonnull Message message, @Nonnull ImageView imageView);

	@Nonnull
	Message sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountException;

	@Nullable
	Message getLastMessage(@Nonnull String chatId);

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

}
