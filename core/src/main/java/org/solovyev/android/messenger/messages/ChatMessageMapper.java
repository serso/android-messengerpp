package org.solovyev.android.messenger.messages;

import android.database.Cursor;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatMessageImpl;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.RealmEntityMapper;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:15 PM
 */
public class ChatMessageMapper implements Converter<Cursor, ChatMessage> {

    @Nonnull
    private final UserService userService;

    public ChatMessageMapper(@Nonnull UserService userService) {
        this.userService = userService;
    }

    @Nonnull
    @Override
    public ChatMessage convert(@Nonnull Cursor c) {
        final Entity messageEntity = RealmEntityMapper.newInstanceFor(0).convert(c);

        final String chatId = c.getString(3);

        final LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance(messageEntity);
        liteChatMessage.setAuthor(userService.getUserById(EntityImpl.fromEntityId(c.getString(4))));
        if (!c.isNull(5)) {
            final String recipientId = c.getString(5);
            liteChatMessage.setRecipient(userService.getUserById(EntityImpl.fromEntityId(recipientId)));
        }
        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

        liteChatMessage.setSendDate(dateTimeFormatter.parseDateTime(c.getString(6)));
        liteChatMessage.setTitle(c.getString(7));
        liteChatMessage.setBody(c.getString(8));

        return ChatMessageImpl.newInstance(liteChatMessage);
    }
}
