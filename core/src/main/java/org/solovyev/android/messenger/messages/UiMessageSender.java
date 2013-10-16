package org.solovyev.android.messenger.messages;

import android.support.v4.app.FragmentActivity;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.messages.SendMessageAsyncTask.SendingMessage.newSendingMessage;
import static org.solovyev.android.messenger.users.ContactUiEventType.resend_message;
import static org.solovyev.android.messenger.users.ContactUiEventType.show_composite_user_dialog;

final class UiMessageSender {

	@Nonnull
	private final FragmentActivity activity;

	@Nonnull
	private final Account account;

	@Nonnull
	private final Chat chat;

	@Nullable
	private final User recipient;

	private UiMessageSender(@Nonnull FragmentActivity activity,
							@Nonnull Account account,
							@Nonnull Chat chat,
							@Nullable User recipient) {
		this.activity = activity;
		this.account = account;
		this.chat = chat;
		this.recipient = recipient;
	}

	@Nullable
	public static Message trySendMessage(@Nonnull FragmentActivity activity,
										 @Nonnull Account account,
										 @Nonnull Chat chat,
										 @Nullable User recipient,
										 @Nonnull String message) {
		final UiMessageSender sender = new UiMessageSender(activity, account, chat, recipient);
		return sender.trySendMessage(message);
	}

	@Nullable
	private Message trySendMessage(@Nonnull String message) {
		if (canSendMessage(message)) {
			final SendMessageAsyncTask task = new SendMessageAsyncTask(activity, chat);
			final SendMessageAsyncTask.SendingMessage sendingMessage = newSendingMessage(account, message, chat, recipient);
			final MutableMessage result = sendingMessage.createMessage();
			task.executeInParallel(sendingMessage);
			return result;
		} else {
			return null;
		}
	}

	private boolean canSendMessage(@Nonnull String message) {
		final boolean result;

		if (!Strings.isEmpty(message)) {
			if (chat.isPrivate()) {
				result = canSendMessageToUser(getContact(chat.getSecondUser()));
			} else {
				result = true;
			}
		} else {
			result = false;
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
				getEventManager(activity).fire(show_composite_user_dialog.newEvent(contact, resend_message));
			}
		}

		return result;
	}
}
