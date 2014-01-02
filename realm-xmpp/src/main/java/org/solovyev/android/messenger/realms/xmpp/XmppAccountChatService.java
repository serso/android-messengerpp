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

package org.solovyev.android.messenger.realms.xmpp;

import com.google.common.base.Function;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.MutableChat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccount.toMessages;

class XmppAccountChatService extends AbstractXmppAccountService implements AccountChatService {

	public XmppAccountChatService(@Nonnull XmppAccount account, @Nonnull XmppConnectionAware connectionAware) {
		super(account, connectionAware);
	}

	@Nonnull
	@Override
	public List<Message> getMessages() throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<List<Message>>() {
			@Override
			public List<Message> call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
				final OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
				try {
					if (offlineManager.supportsFlexibleRetrieval()) {
						return transform(toMessages(getAccount(), offlineManager.getMessages()), new Function<MutableMessage, Message>() {
							@Override
							public Message apply(@Nullable MutableMessage message) {
								return message;
							}
						});
					}
				} catch (XMPPException e) {
					// ok, not supported by server
				}

				return Collections.emptyList();
			}
		});
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

	@Nullable
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		return doOnConnection(new XmppMessengerSender(chat, message, getAccount()));
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public MutableChat newPrivateChat(@Nonnull final Entity accountChat, @Nonnull String accountUserId1, @Nonnull final String accountUserId2) throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<MutableChat>() {
			@Override
			public MutableChat call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
				org.jivesoftware.smack.Chat smackChat = connection.getChatManager().createChat(accountUserId2, accountChat.getEntityId(), new XmppMessageListener(getAccount(), accountChat));
				return XmppAccount.toAccountChat(smackChat, Collections.<org.jivesoftware.smack.packet.Message>emptyList(), getAccount()).getChat();
			}
		});
	}

	@Override
	public boolean markMessageRead(@Nonnull Message message) throws AccountConnectionException {
		return true;
	}
}
