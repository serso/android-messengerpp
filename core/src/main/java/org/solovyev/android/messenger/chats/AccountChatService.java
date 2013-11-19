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

import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface AccountChatService {

	@Nonnull
	List<? extends Message> getMessages() throws AccountConnectionException;

	@Nonnull
	List<Message> getNewerMessagesForChat(@Nonnull String accountChatId) throws AccountConnectionException;

	@Nonnull
	List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull Integer offset) throws AccountConnectionException;

	@Nonnull
	List<AccountChat> getChats() throws AccountConnectionException;

	/**
	 * Method sends message to the account and, if possible, returns message id. If message id could not be returned
	 * (due, for example, to the asynchronous nature of realm) - null is returned (in that case realm connection must receive message id)
	 *
	 * @param chat    chat in which message was created
	 * @param message message to be sent
	 * @return message id of sent message if possible
	 */
	@Nullable
	String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException;

	void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException;

	@Nonnull
	MutableChat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) throws AccountConnectionException;
}
