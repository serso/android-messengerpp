package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;

import java.util.Collections;

import javax.annotation.Nonnull;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatService;

final class XmppChatListener implements ChatManagerListener {

	@Nonnull
	private Account account;

	public XmppChatListener(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public void chatCreated(@Nonnull Chat chat, boolean createdLocally) {
		Log.i("M++/Xmpp", "Chat created!");

		if (!createdLocally) {
			ApiChat newChat = XmppAccount.toApiChat(chat, Collections.<Message>emptyList(), account);
			try {
				newChat = getChatService().saveChat(account.getUser().getEntity(), newChat);
				chat.addMessageListener(new XmppMessageListener(account, newChat.getChat().getEntity()));
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
