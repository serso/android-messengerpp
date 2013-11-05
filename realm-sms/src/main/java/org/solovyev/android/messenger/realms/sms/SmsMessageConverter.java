package org.solovyev.android.messenger.realms.sms;

import android.database.Cursor;

import javax.annotation.Nonnull;

import android.util.Log;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.MessageState;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.common.Converter;

import static org.solovyev.android.messenger.App.getChatService;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.messages.Messages.newMessage;
import static org.solovyev.android.messenger.realms.sms.SmsAccount.TAG;
import static org.solovyev.common.text.Strings.isEmpty;

class SmsMessageConverter implements Converter<Cursor, MutableMessage> {

	@Nonnull
	private final SmsAccount account;

	@Nonnull
	private final MessageService messageService;

	SmsMessageConverter(@Nonnull SmsAccount account, @Nonnull MessageService messageService) {
		this.account = account;
		this.messageService = messageService;
	}

	@Nonnull
	@Override
	public MutableMessage convert(@Nonnull Cursor cursor) {
		final Entity user = account.getUser().getEntity();

		final String messageId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

		final Entity entity;
		if (!isEmpty(messageId)) {
			entity = account.newMessageEntity(messageId);
		} else {
			entity = generateEntity(account);
		}

		final MutableMessage message = newMessage(entity);

		final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
		if (!isEmpty(body)) {
			message.setBody(body);
		} else {
			throw new IllegalArgumentException("Body must not be empty");
		}

		final String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
		final SmsAccountConnection connection = account.getAccountConnection();
		final Entity participant;
		if(connection == null) {
			throw new IllegalArgumentException();
		} else {
			participant = connection.findOrCreateContact(address, getUserService().getUserContacts(user)).getEntity();
		}

		message.setRead(getBoolean(cursor, "read"));
		message.setSendDate(getDate(cursor, "date"));
		final boolean incoming = getBoolean(cursor, "type");
		if (incoming) {
			message.setAuthor(participant);
			message.setRecipient(user);
			message.setState(MessageState.received);
		} else {
			message.setAuthor(user);
			message.setRecipient(participant);
			message.setState(MessageState.sent);
		}

		message.setChat(getChatService().getPrivateChatId(user, participant));

		final Message sameMessage = messageService.getSameMessage(message.getBody(), message.getSendDate(), message.getAuthor(), message.getRecipient());
		if (sameMessage != null) {
			// we cannot rely on message ids in SMS realm => need to use some heuristics to determine if message has already been merged
			Log.i(TAG, "Message already merged: body: " + message.getBody() + ", send date: " + message.getSendDate() + ", author: " + message.getAuthor() + ", recipient: " + message.getRecipient());
			throw new IllegalArgumentException("Already merged");
		}

		return message;
	}

	@Nonnull
	private DateTime getDate(@Nonnull Cursor cursor, @Nonnull String columnName) {
		final long millis = Long.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(columnName)));
		return new DateTime(millis);
	}

	private boolean getBoolean(@Nonnull Cursor cursor, @Nonnull String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName)).equals("1");
	}
}
