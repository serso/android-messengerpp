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

import android.database.Cursor;

import java.util.List;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.Messages.newMessage;

public class MessageMapper implements Converter<Cursor, Message> {

	@Nonnull
	private final MessageDao dao;

	public MessageMapper(@Nonnull MessageDao dao) {
		this.dao = dao;
	}

	@Nonnull
	@Override
	public Message convert(@Nonnull Cursor cursor) {
		final Entity entity = EntityMapper.newInstanceFor(0).convert(cursor);

		final MutableMessage message = newMessage(entity);
		message.setChat(newEntityFromEntityId(cursor.getString(3)));
		message.setAuthor(newEntityFromEntityId(cursor.getString(4)));
		if (!cursor.isNull(5)) {
			final String recipientId = cursor.getString(5);
			message.setRecipient(newEntityFromEntityId(recipientId));
		}
		message.setState(MessageState.valueOf(cursor.getString(11)));
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		message.setSendDate(dateTimeFormatter.parseDateTime(cursor.getString(6)));
		final Long sendTime = cursor.getLong(7);
		message.setTitle(cursor.getString(8));
		message.setBody(cursor.getString(9));

		final boolean read = cursor.getInt(10) == 1;
		message.setRead(read);

		message.setProperties(dao.readPropertiesById(entity.getEntityId()));

		return message;
	}
}
