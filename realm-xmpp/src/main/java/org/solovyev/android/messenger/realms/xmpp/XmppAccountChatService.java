package org.solovyev.android.messenger.realms.xmpp;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableChatMessage;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:17 PM
 */
class XmppAccountChatService extends AbstractXmppRealmService implements AccountChatService {

	public XmppAccountChatService(@Nonnull XmppAccount realm, @Nonnull XmppConnectionAware connectionAware) {
		super(realm, connectionAware);
	}

	@Nonnull
	@Override
	public List<ChatMessage> getChatMessages(@Nonnull String accountUserId) throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<List<ChatMessage>>() {
			@Override
			public List<ChatMessage> call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
				final OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
				try {
					if (offlineManager.supportsFlexibleRetrieval()) {
						return XmppAccount.toMessages(getAccount(), offlineManager.getMessages());
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
	public List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ApiChat> getUserChats(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message) throws AccountConnectionException {
		return doOnConnection(new MessengerSender(chat, message, getAccount()));
	}

	@Override
	public void beforeSendChatMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableChatMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull final Entity accountChat, @Nonnull String accountUserId1, @Nonnull final String accountUserId2) throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<Chat>() {
			@Override
			public Chat call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
				org.jivesoftware.smack.Chat smackChat = connection.getChatManager().createChat(accountUserId2, accountChat.getEntityId(), new XmppMessageListener(getAccount(), accountChat));
				return XmppAccount.toApiChat(smackChat, Collections.<Message>emptyList(), getAccount()).getChat();
			}
		});
	}

	private static final class MessengerSender implements XmppConnectedCallable<String> {

		@Nonnull
		private final Chat chat;

		@Nonnull
		private final ChatMessage message;

		@Nonnull
		private final Account account;

		private MessengerSender(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull Account account) {
			this.chat = chat;
			this.message = message;
			this.account = account;
		}

		@Override
		public String call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
			final ChatManager chatManager = connection.getChatManager();

			final Entity accountChat = chat.getEntity();
			org.jivesoftware.smack.Chat smackChat = chatManager.getThreadChat(accountChat.getAccountEntityId());
			if (smackChat == null) {
				// smack forget about chat ids after restart => need to create chat here
				smackChat = chatManager.createChat(chat.getSecondUser().getAccountEntityId(), accountChat.getAccountEntityId(), new XmppMessageListener(account, accountChat));
			} else {
				// todo serso: remove if unnecessary
				smackChat.addMessageListener(new XmppMessageListener(account, accountChat));
			}

			smackChat.sendMessage(message.getBody());

			return null;
		}
	}
}
