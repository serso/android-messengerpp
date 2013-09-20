package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.realms.AbstractAccount;
import org.solovyev.android.messenger.realms.AccountState;
import org.solovyev.android.messenger.realms.Account;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class XmppAccount extends AbstractAccount<XmppAccountConfiguration> {

	private static final String TAG = XmppAccount.class.getSimpleName();

	public XmppAccount(@Nonnull String id,
					   @Nonnull RealmDef realmDef,
					   @Nonnull User user,
					   @Nonnull XmppAccountConfiguration configuration,
					   @Nonnull AccountState state) {
		super(id, realmDef, user, configuration, state);
	}

	@Nonnull
	@Override
	protected RealmConnection newRealmConnection0(@Nonnull Context context) {
		return new XmppRealmConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		final StringBuilder sb = new StringBuilder();

		sb.append(context.getText(getRealmDef().getNameResId()));
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
		XmppConnectionAware realmAware = getRealmConnection();
		if (realmAware == null) {
			realmAware = TemporaryXmppConnectionAware.newInstance(this);
			Log.w(TAG, "Creation of temporary xmpp connection!");
		}
		return realmAware;
	}

	@Nullable
	protected XmppRealmConnection getRealmConnection() {
		return (XmppRealmConnection) super.getRealmConnection();
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
		return MessengerApplication.getServiceLocator().getChatService();
	}

	@Nonnull
	private static ChatMessageService getChatMessageService() {
		return MessengerApplication.getServiceLocator().getChatMessageService();
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
