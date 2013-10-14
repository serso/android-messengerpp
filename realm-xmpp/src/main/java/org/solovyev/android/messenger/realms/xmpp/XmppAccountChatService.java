package org.solovyev.android.messenger.realms.xmpp;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
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
	public List<Message> getMessages(@Nonnull String accountUserId) throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<List<Message>>() {
			@Override
			public List<Message> call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
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
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ApiChat> getChats(@Nonnull String accountUserId) {
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
	public Chat newPrivateChat(@Nonnull final Entity accountChat, @Nonnull String accountUserId1, @Nonnull final String accountUserId2) throws AccountConnectionException {
		return doOnConnection(new XmppConnectedCallable<Chat>() {
			@Override
			public Chat call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
				org.jivesoftware.smack.Chat smackChat = connection.getChatManager().createChat(accountUserId2, accountChat.getEntityId(), new XmppMessageListener(getAccount(), accountChat));
				return XmppAccount.toApiChat(smackChat, Collections.<org.jivesoftware.smack.packet.Message>emptyList(), getAccount()).getChat();
			}
		});
	}

}
