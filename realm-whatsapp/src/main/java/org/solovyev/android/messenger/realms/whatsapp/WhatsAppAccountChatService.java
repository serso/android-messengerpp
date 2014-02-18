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

package org.solovyev.android.messenger.realms.whatsapp;

import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;

final class WhatsAppAccountChatService implements AccountChatService {
	@Nonnull
	private final WhatsAppAccount account;

	public WhatsAppAccountChatService(@Nonnull WhatsAppAccount account) {
		this.account = account;
	}

	@Nonnull
	@Override
	public List<? extends Message> getMessages() throws AccountConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId) throws AccountConnectionException {
		return emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull Integer offset) throws AccountConnectionException {
		return emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats() throws AccountConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Nullable
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public MutableChat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) throws AccountConnectionException {
		return Chats.newPrivateChat(accountChat);
	}

	@Override
	public boolean markMessageRead(@Nonnull Message message) throws AccountConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}
}
