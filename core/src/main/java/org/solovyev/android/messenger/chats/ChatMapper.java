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
		final Entity realmChat = EntityMapper.newInstanceFor(0).convert(c);

		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
		final String lastMessagesSyncDateString = c.getString(3);
		final DateTime lastMessagesSyncDate = lastMessagesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastMessagesSyncDateString);

		final List<AProperty> properties = chatDao.readPropertiesById(realmChat.getEntityId());

		//final List<ChatMessage> chatMessages = chatDao.loadChatMessages(chatId);
		//final List<User> chatParticipants = chatDao.loadChatParticipants(chatId);

		return ChatImpl.newInstance(realmChat, properties, lastMessagesSyncDate);
	}
}