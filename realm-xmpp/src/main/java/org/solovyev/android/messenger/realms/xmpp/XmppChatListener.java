package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.solovyev.android.messenger.realms.xmpp.XmppRealm.TAG;

final class XmppChatListener implements ChatManagerListener {

	@Nonnull
	private Account account;

	public XmppChatListener(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public void chatCreated(@Nonnull org.jivesoftware.smack.Chat chat, boolean createdLocally) {
		Log.d(TAG, "Chat created!");

		if (!createdLocally) {
			final ApiChat newApiChat = XmppAccount.toApiChat(chat, Collections.<Message>emptyList(), account);
			try {
				final Chat newChat = getChatService().saveChat(account.getUser().getEntity(), newApiChat);
				if (newChat != null) {
					chat.addMessageListener(new XmppMessageListener(account, newChat.getEntity()));
				} else {
					Log.e(TAG, "Chat has not been found for thread id: " + chat.getThreadID());
				}
			} catch (AccountException e) {
				App.getExceptionHandler().handleException(e);
			}
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}
}
