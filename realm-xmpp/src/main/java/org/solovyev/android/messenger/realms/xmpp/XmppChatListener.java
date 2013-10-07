package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;

import java.util.Collections;

import javax.annotation.Nonnull;

import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;

final class XmppChatListener implements ChatManagerListener {

	@Nonnull
	private Account account;

	public XmppChatListener(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public void chatCreated(@Nonnull org.jivesoftware.smack.Chat chat, boolean createdLocally) {
		Log.i("M++/Xmpp", "Chat created!");

		if (!createdLocally) {
			final ApiChat newApiChat = XmppAccount.toApiChat(chat, Collections.<Message>emptyList(), account);
			try {
				final Chat newChat = getChatService().saveChat(account.getUser().getEntity(), newApiChat);
				chat.addMessageListener(new XmppMessageListener(account, newChat.getEntity()));
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
