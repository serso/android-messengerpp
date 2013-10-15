package org.solovyev.android.messenger.messages;

import android.content.Context;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.messages.Messages.newOutgoingMessage;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 11:00 PM
 */
public class SendMessageAsyncTask extends MessengerAsyncTask<SendMessageAsyncTask.Input, Void, List<Message>> {

	@Nonnull
	private final Chat chat;

	public SendMessageAsyncTask(@Nonnull Context context, @Nonnull Chat chat) {
		super(context);
		this.chat = chat;
	}

	@Override
	protected List<Message> doWork(@Nonnull List<Input> inputs) {
		final List<Message> result = new ArrayList<Message>(inputs.size());

		try {
			for (Input input : inputs) {
				final Context context = getContext();
				if (context != null) {
					assert chat.equals(input.chat);

					final Message message = input.sendMessage();
					if (message != null) {
						result.add(message);
					}
				}
			}
		} catch (AccountException e) {
			throwException(e);
		}

		return result;
	}

	@Override
	protected void onSuccessPostExecute(@Nullable List<Message> result) {
		if (result != null) {
			//getChatService().fireEvent(ChatEventType.message_added_batch.newEvent(chat, result)); wait remote add
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	@Nonnull
	private static MessageService getMessageService() {
		return App.getMessageService();
	}

	@Nonnull
	private static UserService getUserService() {
		return App.getUserService();
	}

	@Nonnull
	private static AccountService getAccountService() {
		return App.getAccountService();
	}

	public static class Input {

		@Nonnull
		private String message;

		@Nullable
		private String title;

		@Nonnull
		private final List<Object> attachments = new ArrayList<Object>();

		@Nonnull
		private final Chat chat;

		@Nullable
		private final User recipient;

		public Input(@Nonnull String message, @Nonnull Chat chat, @Nullable User recipient) {
			this.message = message;
			this.chat = chat;
			this.recipient = recipient;
		}

		public void setTitle(@Nullable String title) {
			this.title = title;
		}

		public boolean addAttachment(Object attachment) {
			return attachments.add(attachment);
		}

		@Nullable
		public Message sendMessage() throws AccountException {
			final Account account = getAccountService().getAccountById(chat.getEntity().getAccountId());

			final MutableMessage result = newOutgoingMessage(account, chat, message, title);

			// on before send hook
			account.getAccountChatService().beforeSendMessage(chat, recipient, result);

			return getMessageService().sendMessage(chat, result);
		}

	}
}


