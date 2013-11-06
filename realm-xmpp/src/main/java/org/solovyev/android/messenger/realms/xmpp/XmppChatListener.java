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

import android.util.Log;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.solovyev.android.messenger.realms.xmpp.XmppAccount.toAccountChat;
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
			final AccountChat newAccountChat = toAccountChat(chat, Collections.<Message>emptyList(), account);
			try {
				final Chat newChat = getChatService().saveChat(account.getUser().getEntity(), newAccountChat);
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
