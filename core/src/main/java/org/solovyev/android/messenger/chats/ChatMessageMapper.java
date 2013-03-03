package org.solovyev.android.messenger.chats;

import android.database.Cursor;
import javax.annotation.Nonnull;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

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
        final String messageId = c.getString(0);
        final String chatId = c.getString(1);

        final LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance(messageId);
        liteChatMessage.setAuthor(userService.getUserById(RealmEntityImpl.fromEntityId(c.getString(2))));
        if (!c.isNull(3)) {
            final String recipientId = c.getString(3);
            liteChatMessage.setRecipient(userService.getUserById(RealmEntityImpl.fromEntityId(recipientId)));
        }
        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

        liteChatMessage.setSendDate(dateTimeFormatter.parseDateTime(c.getString(4)));
        liteChatMessage.setTitle(c.getString(5));
        liteChatMessage.setBody(c.getString(6));

        return ChatMessageImpl.newInstance(liteChatMessage);
    }
}
