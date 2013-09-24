package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

public final class XmppAccount extends AbstractAccount<XmppAccountConfiguration> {

	private static final String TAG = XmppAccount.class.getSimpleName();

	public XmppAccount(@Nonnull String id,
					   @Nonnull Realm realm,
					   @Nonnull User user,
					   @Nonnull XmppAccountConfiguration configuration,
					   @Nonnull AccountState state) {
		super(id, realm, user, configuration, state);
	}

	@Nonnull
	@Override
	protected AccountConnection newRealmConnection0(@Nonnull Context context) {
		return new XmppAccountConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		final StringBuilder sb = new StringBuilder();

		sb.append(context.getText(getRealm().getNameResId()));
		sb.append("@");
		sb.append(getConfiguration().getServer());

		return sb.toString();
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new XmppAccountUserService(this, getXmppConnectionAware());
	}

	@Nonnull
	private XmppConnectionAware getXmppConnectionAware() {
		XmppConnectionAware realmAware = getAccountConnection();
		if (realmAware == null) {
			realmAware = TemporaryXmppConnectionAware.newInstance(this);
			Log.w(TAG, "Creation of temporary xmpp connection!");
		}
		return realmAware;
	}

	@Nullable
	protected XmppAccountConnection getAccountConnection() {
		return (XmppAccountConnection) super.getAccountConnection();
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new XmppAccountChatService(this, getXmppConnectionAware());
	}

	@Nonnull
	public Entity newUserEntity(@Nonnull String realmUserId) {
		return newEntity(realmUserId);
	}

	@Nonnull
	private Entity newEntity(@Nonnull String realmUserId) {
		final int index = realmUserId.indexOf('/');
		if (index >= 0) {
			return newRealmEntity(realmUserId.substring(0, index));
		} else {
			return newRealmEntity(realmUserId);
		}
	}

	@Nonnull
	public Entity newChatEntity(@Nonnull String realmUserId) {
		return newEntity(realmUserId);
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
	private static ChatMessageService getChatMessageService() {
		return App.getChatMessageService();
	}

	@Nonnull
	static ApiChat toApiChat(@Nonnull Chat smackChat, @Nonnull List<Message> messages, @Nonnull Account account) {
		final User participant = toUser(smackChat.getParticipant(), account);

		final Entity chat;

		final String realmChatId = smackChat.getThreadID();
		if (Strings.isEmpty(realmChatId)) {
			chat = getChatService().getPrivateChatId(account.getUser().getEntity(), participant.getEntity());
		} else {
			chat = account.newChatEntity(realmChatId);
		}

		final List<ChatMessage> chatMessages = toMessages(account, messages);
		final List<User> participants = Arrays.asList(account.getUser(), participant);
		return Chats.newPrivateApiChat(chat, participants, chatMessages);
	}

	@Nonnull
	static List<ChatMessage> toMessages(@Nonnull Account account, @Nonnull Iterable<Message> messages) {
		return toMessages(account, messages.iterator());
	}

	static List<ChatMessage> toMessages(@Nonnull Account account, @Nonnull Iterator<Message> messages) {
		final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

		while (messages.hasNext()) {
			final Message message = messages.next();
			final ChatMessage chatMessage = toChatMessage(message, account);
			if (chatMessage != null) {
				chatMessages.add(chatMessage);
			}
		}
		return chatMessages;
	}

	@Nullable
	private static ChatMessage toChatMessage(@Nonnull Message message, @Nonnull Account account) {
		final String body = message.getBody();
		if (!Strings.isEmpty(body)) {
			final LiteChatMessageImpl liteChatMessage = Messages.newMessage(getChatMessageService().generateEntity(account));
			liteChatMessage.setBody(body);
			liteChatMessage.setAuthor(account.newUserEntity(message.getFrom()));
			liteChatMessage.setRecipient(account.newUserEntity(message.getTo()));
			liteChatMessage.setSendDate(DateTime.now());
			// new message by default unread
			return Messages.newInstance(liteChatMessage, false);
		} else {
			return null;
		}
	}

	@Nonnull
	private static User toUser(@Nonnull String realmUserId, @Nonnull Account account) {
		return Users.newEmptyUser(account.newUserEntity(realmUserId));
	}
}
