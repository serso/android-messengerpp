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

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.MutableAccountChat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jivesoftware.smack.packet.Message.Type.error;
import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.chats.Chats.newPrivateAccountChat;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.messages.MessageState.received;
import static org.solovyev.android.messenger.messages.MessageState.sent;
import static org.solovyev.android.messenger.messages.Messages.newMessage;

public final class XmppAccount extends AbstractAccount<XmppAccountConfiguration> {

	private static final String TAG = newTag(XmppAccount.class.getSimpleName());

	public XmppAccount(@Nonnull String id,
					   @Nonnull Realm realm,
					   @Nonnull User user,
					   @Nonnull XmppAccountConfiguration configuration,
					   @Nonnull AccountState state,
					   @Nonnull AccountSyncData syncData) {
		super(id, realm, user, configuration, state, syncData);
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new XmppAccountConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		final StringBuilder sb = new StringBuilder();

		final Realm realm = getRealm();
		sb.append(context.getText(realm.getNameResId()));
		if (realm instanceof CustomXmppRealm) {
			sb.append("@");
			sb.append(getConfiguration().getServer());
		}

		return sb.toString();
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new XmppAccountUserService(this, getXmppConnectionAware(), App.getUserService());
	}

	@Nonnull
	private XmppConnectionAware getXmppConnectionAware() {
		XmppConnectionAware realmAware = getConnection();
		if (realmAware == null) {
			realmAware = TemporaryXmppConnectionAware.newInstance(this);
			Log.w(TAG, "Creation of temporary xmpp connection!");
		}
		return realmAware;
	}

	@Nullable
	protected XmppAccountConnection getConnection() {
		return (XmppAccountConnection) super.getConnection();
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new XmppAccountChatService(this, getXmppConnectionAware());
	}

	@Nonnull
	public Entity newUserEntity(@Nonnull String accountUserId) {
		return newEntity(accountUserId);
	}

	@Nonnull
	public Entity newEntity(@Nonnull String realmUserId) {
		final int index = realmUserId.indexOf('/');
		if (index >= 0) {
			return super.newEntity(realmUserId.substring(0, index));
		} else {
			return super.newEntity(realmUserId);
		}
	}

	@Nonnull
	public Entity newChatEntity(@Nonnull String accountChatId) {
		return newEntity(accountChatId);
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */


	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	@Nonnull
	static MutableAccountChat toAccountChat(@Nonnull Chat smackChat, @Nonnull List<Message> smackMessages, @Nonnull Account account) {
		final User participant = toUser(smackChat.getParticipant(), account);

		final Entity chat;

		final String accountChatId = smackChat.getThreadID();
		if (Strings.isEmpty(accountChatId)) {
			chat = getChatService().getPrivateChatId(account.getUser().getEntity(), participant.getEntity());
		} else {
			chat = account.newChatEntity(accountChatId);
		}

		final List<MutableMessage> messages = toMessages(account, smackMessages);
		return newPrivateAccountChat(chat, account.getUser(), participant, messages);
	}

	@Nonnull
	static List<MutableMessage> toMessages(@Nonnull Account account, @Nonnull Iterable<Message> smackMessages) {
		return toMessages(account, smackMessages.iterator());
	}

	static List<MutableMessage> toMessages(@Nonnull Account account, @Nonnull Iterator<Message> smackMessages) {
		final List<MutableMessage> messages = new ArrayList<MutableMessage>();

		while (smackMessages.hasNext()) {
			final Message smackMessage = smackMessages.next();
			if (smackMessage.getType() != error) {
				final MutableMessage message = toMessage(smackMessage, account);
				if (message != null) {
					messages.add(message);
				}
			}
		}
		return messages;
	}

	@Nullable
	private static MutableMessage toMessage(@Nonnull Message xmppMessage, @Nonnull Account account) {
		final String body = xmppMessage.getBody();
		if (!Strings.isEmpty(body)) {
			final Entity user = account.getUser().getEntity();

			final MutableMessage message = newMessage(generateEntity(account));
			message.setBody(body);

			final String from = xmppMessage.getFrom();
			final Entity author = account.newUserEntity(from);

			final String to = xmppMessage.getTo();
			Entity recipient = account.newUserEntity(to);
			if (user.equals(author) || user.equals(recipient)) {
				// user found
				message.setAuthor(author);
				message.setRecipient(recipient);

				if (account.getUser().equals(author)) {
					message.setState(sent);
					message.setChat(getChatService().getPrivateChatId(user, recipient));
				} else {
					message.setState(received);
					message.setChat(getChatService().getPrivateChatId(user, author));
				}
			} else {
				// fallback: use user as recipient as these messages have come from remote server => most probably thy are incoming
				message.setAuthor(author);
				message.setRecipient(user);

				message.setState(received);
				message.setChat(getChatService().getPrivateChatId(user, author));
			}

			message.setSendDate(DateTime.now());
			message.setRead(false);
			return message;
		} else {
			return null;
		}
	}

	@Nonnull
	private static User toUser(@Nonnull String accountUserId, @Nonnull Account account) {
		return Users.newEmptyUser(account.newUserEntity(accountUserId));
	}
}
