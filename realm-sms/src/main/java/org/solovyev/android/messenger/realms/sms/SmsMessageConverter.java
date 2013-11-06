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

package org.solovyev.android.messenger.realms.sms;

import android.database.Cursor;
import android.util.Log;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.MessageState;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

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
		if (connection == null) {
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
