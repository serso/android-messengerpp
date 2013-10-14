package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.users.ContactUiEventType.resend_message;
import static org.solovyev.android.messenger.users.ContactUiEventType.show_composite_user_dialog;

final class UiMessageSender {

	@Nonnull
	private final FragmentActivity activity;

	@Nonnull
	private final EditText messageEditText;

	@Nonnull
	private final Account account;

	@Nonnull
	private final Chat chat;

	@Nullable
	private final User recipient;

	private UiMessageSender(@Nonnull FragmentActivity activity,
							@Nonnull EditText messageEditText,
							@Nonnull Account account,
							@Nonnull Chat chat,
							@Nullable User recipient) {
		this.activity = activity;
		this.messageEditText = messageEditText;
		this.account = account;
		this.chat = chat;
		this.recipient = recipient;
	}

	public static boolean trySendMessage(@Nonnull FragmentActivity activity,
												 @Nonnull EditText messageEditText,
												 @Nonnull Account account,
												 @Nonnull Chat chat,
												 @Nullable User recipient) {
		final UiMessageSender sender = new UiMessageSender(activity, messageEditText, account, chat, recipient);
		return sender.trySendMessage();
	}

	public boolean trySendMessage() {
		final String messageText = Strings.toHtml(messageEditText.getText());
		if (!Strings.isEmpty(messageText)) {
			return trySendMessage(messageEditText, messageText);
		} else {
			return false;
		}
	}

	private boolean trySendMessage(@Nonnull EditText messageBody, @Nonnull String messageText) {
		if (canSendMessage()) {
			final SendMessageAndUpdateEditTextAsyncTask task = new SendMessageAndUpdateEditTextAsyncTask(activity, messageBody, chat);
			final SendMessageAsyncTask.Input input = new SendMessageAsyncTask.Input(account.getUser(), messageText, chat, recipient);
			task.executeInParallel(input);
			return true;
		} else {
			return false;
		}
	}

	private boolean canSendMessage() {
		boolean result = true;

		if (chat.isPrivate()) {
			result = canSendMessageToUser(getContact(chat.getSecondUser()));
		}

		return result;
	}

	@Nonnull
	private User getContact(@Nonnull Entity contactEntity) {
		if (recipient == null) {
			return getUserService().getUserById(contactEntity);
		} else {
			if (recipient.getEntity().equals(contactEntity)) {
				return recipient;
			} else {
				return getUserService().getUserById(contactEntity);
			}
		}
	}

	private boolean canSendMessageToUser(@Nonnull User contact) {
		boolean result = true;

		if (account.isCompositeUser(contact)) {
			if (!account.isCompositeUserDefined(contact)) {
				result = false;
				App.getEventManager(activity).fire(show_composite_user_dialog.newEvent(contact, resend_message));
			}
		}

		return result;
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	private static class SendMessageAndUpdateEditTextAsyncTask extends SendMessageAsyncTask {

		@Nonnull
		private final WeakReference<EditText> messageBodyRef;

		public SendMessageAndUpdateEditTextAsyncTask(@Nonnull Activity activity, @Nonnull EditText messageBody, @Nonnull Chat chat) {
			super(activity, chat);
			this.messageBodyRef = new WeakReference<EditText>(messageBody);
		}

		@Override
		protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
			super.onSuccessPostExecute(result);
			final EditText messageBody = messageBodyRef.get();
			if (messageBody != null) {
				messageBody.setText("");
			}
		}
	}
}
