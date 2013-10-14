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
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.*;
import org.solovyev.android.messenger.realms.Realm;
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

import static org.jivesoftware.smack.packet.Message.Type.error;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.messages.MessageState.received;
import static org.solovyev.android.messenger.messages.MessageState.sent;
import static org.solovyev.android.messenger.messages.Messages.newMessage;

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
	protected AccountConnection createConnection(@Nonnull Context context) {
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
	static ApiChat toApiChat(@Nonnull Chat smackChat, @Nonnull List<Message> smackMessages, @Nonnull Account account) {
		final User participant = toUser(smackChat.getParticipant(), account);

		final Entity chat;

		final String accountChatId = smackChat.getThreadID();
		if (Strings.isEmpty(accountChatId)) {
			chat = getChatService().getPrivateChatId(account.getUser().getEntity(), participant.getEntity());
		} else {
			chat = account.newChatEntity(accountChatId);
		}

		final List<org.solovyev.android.messenger.messages.Message> messages = toMessages(account, smackMessages);
		final List<User> participants = Arrays.asList(account.getUser(), participant);
		return Chats.newPrivateApiChat(chat, participants, messages);
	}

	@Nonnull
	static List<org.solovyev.android.messenger.messages.Message> toMessages(@Nonnull Account account, @Nonnull Iterable<Message> smackMessages) {
		return toMessages(account, smackMessages.iterator());
	}

	static List<org.solovyev.android.messenger.messages.Message> toMessages(@Nonnull Account account, @Nonnull Iterator<Message> smackMessages) {
		final List<org.solovyev.android.messenger.messages.Message> messages = new ArrayList<org.solovyev.android.messenger.messages.Message>();

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
			final MutableMessage message = newMessage(generateEntity(account));
			message.setBody(body);
			final Entity author = account.newUserEntity(xmppMessage.getFrom());
			message.setAuthor(author);
			final Entity recipient = account.newUserEntity(xmppMessage.getTo());
			message.setRecipient(recipient);
			message.setChat(getChatService().getPrivateChatId(author, recipient));
			message.setSendDate(DateTime.now());
			if(account.getUser().equals(author)) {
				message.setState(sent);
			} else {
				message.setState(received);
			}
			message.setRead(false);
			return message;
		} else {
			return null;
		}
	}

	@Nonnull
	private static User toUser(@Nonnull String realmUserId, @Nonnull Account account) {
		return Users.newEmptyUser(account.newUserEntity(realmUserId));
	}
}
