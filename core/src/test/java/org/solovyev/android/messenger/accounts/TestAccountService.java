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

package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:15 PM
 */
public class TestAccountService implements AccountUserService, AccountChatService {
	@Nullable
	@Override
	public User getUserById(@Nonnull String accountUserId) {
		return null;
	}

	@Nonnull
	@Override
	public List<User> getContacts() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<User> getOnlineUsers() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}

	@Nonnull
	@Override
	public List<Message> getMessages() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) {
		return "test_message_id";
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public MutableChat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) {
		return Chats.newPrivateChat(accountChat);
	}
}
