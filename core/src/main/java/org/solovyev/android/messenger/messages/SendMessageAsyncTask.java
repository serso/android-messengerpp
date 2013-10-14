package org.solovyev.android.messenger.messages;

import android.content.Context;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.MessageDirection;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.messages.MessageState.sending;
import static org.solovyev.android.messenger.messages.Messages.newMessage;
import static org.solovyev.android.messenger.messages.Messages.newChatMessage;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 11:00 PM
 */
public class SendMessageAsyncTask extends MessengerAsyncTask<SendMessageAsyncTask.Input, Void, List<ChatMessage>> {

	@Nonnull
	private final Chat chat;

	public SendMessageAsyncTask(@Nonnull Context context, @Nonnull Chat chat) {
		super(context);
		this.chat = chat;
	}

	@Override
	protected List<ChatMessage> doWork(@Nonnull List<Input> inputs) {
		final List<ChatMessage> result = new ArrayList<ChatMessage>(inputs.size());

		try {
			for (Input input : inputs) {
				final Context context = getContext();
				if (context != null) {
					assert chat.equals(input.chat);

					final ChatMessage message = input.sendChatMessage();
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
	protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
		if (result != null) {
			//getChatService().fireEvent(ChatEventType.message_added_batch.newEvent(chat, result)); wait remote add
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	@Nonnull
	private static MessageService getChatMessageService() {
		return App.getChatMessageService();
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
		private final User author;

		@Nonnull
		private String message;

		@Nullable
		private String title;

		@Nonnull
		private final List<Object> attachments = new ArrayList<Object>();

		@Nonnull
		private final List<Message> fwdMessages = new ArrayList<Message>();

		@Nonnull
		private final Chat chat;

		@Nullable
		private final User recipient;

		public Input(@Nonnull User author, @Nonnull String message, @Nonnull Chat chat, @Nullable User recipient) {
			this.author = author;
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

		public boolean addFwdMessage(@Nonnull Message fwdMessage) {
			return fwdMessages.add(fwdMessage);
		}

		@Nullable
		public ChatMessage sendChatMessage() throws AccountException {
			final Account account = getAccountService().getAccountById(author.getEntity().getAccountId());

			final MessageImpl message = newMessage(generateEntity(account));
			message.setChat(chat.getEntity());
			message.setAuthor(author.getEntity());
			message.setBody(this.message);

			if (chat.isPrivate()) {
				final Entity secondUser = chat.getSecondUser();
				message.setRecipient(secondUser);
			}

			message.setTitle(title == null ? "" : title);
			message.setSendDate(DateTime.now());
			message.setState(sending);

			final ChatMessageImpl chatMessage = newChatMessage(message, true);
			chatMessage.setDirection(MessageDirection.out);
			for (Message fwdMessage : fwdMessages) {
				chatMessage.addFwdMessage(fwdMessage);
			}

			account.getAccountChatService().beforeSendChatMessage(chat, recipient, chatMessage);

			return getChatMessageService().sendMessage(author.getEntity(), chat, chatMessage);
		}

	}
}


