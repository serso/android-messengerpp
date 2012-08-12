package org.solovyev.android.messenger.chats;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.AProperty;
import org.solovyev.common.Converter;

import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:42 PM
 */
public class ChatMapper implements Converter<Cursor, Chat> {

    @NotNull
    private final ChatDao chatDao;

    public ChatMapper(@NotNull ChatDao chatDao) {
        this.chatDao = chatDao;
    }

    @NotNull
    @Override
    public Chat convert(@NotNull Cursor c) {
        final String chatId = c.getString(0);
        final Integer messagesCount = c.getInt(1);

        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
        final String lastMessagesSyncDateString = c.getString(2);
        final DateTime lastMessagesSyncDate = lastMessagesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastMessagesSyncDateString);

        final List<AProperty> properties = chatDao.loadChatPropertiesById(chatId);

        //final List<ChatMessage> chatMessages = chatDao.loadChatMessages(chatId);
        //final List<User> chatParticipants = chatDao.loadChatParticipants(chatId);

        return new ChatImpl(chatId, messagesCount, properties, lastMessagesSyncDate);
    }
}