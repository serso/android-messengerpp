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

package org.solovyev.android.messenger.chats;

import android.database.Cursor;

import java.util.List;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:42 PM
 */
public class ChatMapper implements Converter<Cursor, Chat> {

	@Nonnull
	private final ChatDao chatDao;

	public ChatMapper(@Nonnull ChatDao chatDao) {
		this.chatDao = chatDao;
	}

	@Nonnull
	@Override
	public Chat convert(@Nonnull Cursor c) {
		final Entity chat = EntityMapper.newInstanceFor(0).convert(c);

		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
		final String lastMessagesSyncDateString = c.getString(3);
		final DateTime lastMessagesSyncDate = lastMessagesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastMessagesSyncDateString);

		final List<AProperty> properties = chatDao.readPropertiesById(chat.getEntityId());

		return Chats.newChat(chat, properties, lastMessagesSyncDate);
	}
}