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

package org.solovyev.android.messenger.messages;

import android.content.Context;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.messages.Messages.newOutgoingMessage;


public class SendMessageAsyncTask extends MessengerAsyncTask<SendMessageAsyncTask.SendingMessage, Void, List<Message>> {

	@Nonnull
	private final Chat chat;

	public SendMessageAsyncTask(@Nonnull Context context, @Nonnull Chat chat) {
		super(context);
		this.chat = chat;
	}

	@Override
	protected List<Message> doWork(@Nonnull List<SendingMessage> sendingMessages) {
		final List<Message> result = new ArrayList<Message>(sendingMessages.size());

		try {
			for (SendingMessage sendingMessage : sendingMessages) {
				final Context context = getContext();
				if (context != null) {
					assert chat.equals(sendingMessage.chat);
					result.add(sendingMessage.sendMessage());
				}
			}
		} catch (AccountException e) {
			throwException(e);
		}

		return result;
	}

	@Override
	protected void onSuccessPostExecute(@Nullable List<Message> messages) {
	}

	@Nonnull
	private static MessageService getMessageService() {
		return App.getMessageService();
	}

	public static class SendingMessage {

		@Nonnull
		private final Account account;

		@Nonnull
		private String text;

		@Nullable
		private String title;

		@Nonnull
		private final List<Object> attachments = new ArrayList<Object>();

		@Nonnull
		private final Chat chat;

		@Nullable
		private final User recipient;

		@Nullable
		private MutableMessage message;

		private SendingMessage(@Nonnull Account account, @Nonnull String text, @Nonnull Chat chat, @Nullable User recipient) {
			this.account = account;
			this.text = text;
			this.chat = chat;
			this.recipient = recipient;
		}

		@Nonnull
		public static SendingMessage newSendingMessage(@Nonnull Account account, @Nonnull String message, @Nonnull Chat chat, @Nullable User recipient) {
			return new SendingMessage(account, message, chat, recipient);
		}

		public void setTitle(@Nullable String title) {
			this.title = title;
		}

		public boolean addAttachment(Object attachment) {
			return attachments.add(attachment);
		}

		@Nonnull
		public Message sendMessage() throws AccountException {
			final MutableMessage m = createMessage();

			// on before send hook
			account.getAccountChatService().beforeSendMessage(chat, recipient, m);

			final Message result = getMessageService().sendMessage(chat, m);
			App.getChatService().removeDraftMessage(chat);
			return result;
		}

		@Nonnull
		public MutableMessage createMessage() {
			if (message == null) {
				message = newOutgoingMessage(account, chat, text, title);
			}

			return message;
		}
	}
}


