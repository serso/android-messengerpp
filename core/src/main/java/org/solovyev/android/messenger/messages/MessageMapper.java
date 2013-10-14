package org.solovyev.android.messenger.messages;

import android.database.Cursor;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.Messages.newMessage;
import static org.solovyev.android.messenger.messages.Messages.newChatMessage;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:15 PM
 */
public class MessageMapper implements Converter<Cursor, ChatMessage> {

	@Nonnull
	private final UserService userService;

	public MessageMapper(@Nonnull UserService userService) {
		this.userService = userService;
	}

	@Nonnull
	@Override
	public ChatMessage convert(@Nonnull Cursor c) {
		final Entity messageEntity = EntityMapper.newInstanceFor(0).convert(c);

		final MessageImpl message = newMessage(messageEntity);
		message.setChat(newEntityFromEntityId(c.getString(3)));
		message.setAuthor(newEntityFromEntityId(c.getString(4)));
		if (!c.isNull(5)) {
			final String recipientId = c.getString(5);
			message.setRecipient(newEntityFromEntityId(recipientId));
		}
		message.setState(MessageState.valueOf(c.getString(11)));
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		message.setSendDate(dateTimeFormatter.parseDateTime(c.getString(6)));
		final Long sendTime = c.getLong(7);
		message.setTitle(c.getString(8));
		message.setBody(c.getString(9));
		final boolean read = c.getInt(10) == 1;


		return newChatMessage(message, read);
	}
}
