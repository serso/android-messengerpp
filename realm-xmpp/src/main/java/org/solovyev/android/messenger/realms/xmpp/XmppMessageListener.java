package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableMessage;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccount.toMessages;

/**
 * User: serso
 * Date: 3/12/13
 * Time: 8:11 PM
 */
final class XmppMessageListener implements ChatStateListener {

	@Nonnull
	private Account account;

	@Nonnull
	private final Entity chat;

	XmppMessageListener(@Nonnull Account account, @Nonnull Entity chat) {
		this.account = account;
		this.chat = chat;
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		Log.i("M++/Xmpp", "Message created: " + message.getBody());
		final List<MutableMessage> messages = toMessages(account, asList(message));
		if (!messages.isEmpty()) {
			getChatService().saveMessages(this.chat, messages);
		} else {
			/**
			 * Some special messages sent by another client like 'Composing' and 'Pausing'.
			 * 'Composing' message will be processed in {@link XmppMessageListener#stateChanged(org.jivesoftware.smack.Chat, org.jivesoftware.smackx.ChatState)} method
			 */
		}
	}

	@Override
	public void stateChanged(Chat smackChat, ChatState state) {
		Log.i("M++/Xmpp", "Chat state changed: " + state);
		if (state == ChatState.composing || state == ChatState.paused) {
			final org.solovyev.android.messenger.chats.Chat chat = getChatService().getChatById(this.chat);
			if (chat != null && chat.isPrivate()) {
				final Entity participant = chat.getSecondUser();
				if (state == ChatState.composing) {
					getChatService().fireEvent(ChatEventType.user_is_typing.newEvent(chat, participant));
				} else {
					getChatService().fireEvent(ChatEventType.user_is_not_typing.newEvent(chat, participant));
				}
			}
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		XmppMessageListener that = (XmppMessageListener) o;

		if (!chat.equals(that.chat)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return chat.hashCode();
	}
}
