package org.solovyev.android.messenger.messages;

import android.text.Html;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.TimeZone;

import static org.joda.time.DateTime.now;
import static org.joda.time.format.DateTimeFormat.shortDate;
import static org.joda.time.format.DateTimeFormat.shortTime;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:50 PM
 */
public final class Messages {

	private Messages() {
		throw new AssertionError();
	}

	@Nonnull
	public static CharSequence getMessageTime(@Nonnull ChatMessage message) {
		final DateTimeZone localTimeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());

		final DateTime localSendDateTime = message.getLocalSendDateTime();
		final LocalDate localSendDate = message.getLocalSendDate();

		final LocalDate localToday = now(localTimeZone).toLocalDate();
		final LocalDate localYesterday = localToday.minusDays(1);

		if (localSendDate.toDateTimeAtStartOfDay().compareTo(localToday.toDateTimeAtStartOfDay()) == 0) {
			// today
			// print time
			return shortTime().print(localSendDateTime);
		} else if (localSendDate.toDateTimeAtStartOfDay().compareTo(localYesterday.toDateTimeAtStartOfDay()) == 0) {
			// yesterday
			// todo serso: translate
			return "Yesterday";// + ", " + DateTimeFormat.shortTime().print(sendDate);
		} else {
			// the days before yesterday
			return shortDate().print(localSendDateTime);
		}
	}

	@Nonnull
	public static CharSequence getMessageTitle(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
		final String authorName = getMessageAuthorDisplayName(chat, message, user);
		if (Strings.isEmpty(authorName)) {
			return Html.fromHtml(message.getBody());
		} else {
			return authorName + ": " + Html.fromHtml(message.getBody());
		}
	}

	@Nonnull
	private static String getMessageAuthorDisplayName(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
		final Entity author = message.getAuthor();
		if (user.getEntity().equals(author)) {
			// todo serso: translate
			return "Me";
		} else {
			if (!chat.isPrivate()) {
				return Users.getDisplayNameFor(author);
			} else {
				return "";
			}
		}
	}

	@Nonnull
	public static LiteChatMessage newEmptyMessage(@Nonnull String messageId) {
		return LiteChatMessageImpl.newInstance(Entities.newEntityFromEntityId(messageId));
	}

	@Nonnull
	public static LiteChatMessageImpl newMessage(@Nonnull Entity entity) {
		return LiteChatMessageImpl.newInstance(entity);
	}

	public static ChatMessageImpl newInstance(@Nonnull LiteChatMessage liteChatMessage, boolean read) {
		return ChatMessageImpl.newInstance(liteChatMessage, read);
	}

	@Nonnull
	public static ChatMessage newEmpty(@Nonnull String messageId) {
		return newInstance(newEmptyMessage(messageId), false);
	}

	public static int compareSendDatesLatestFirst(@Nullable ChatMessage lm, @Nullable ChatMessage rm) {
		if(lm == null && rm == null) {
			return 0;
		} else if (lm == null) {
			return 1;
		} else if (rm == null) {
			return -1;
		} else {
			return -lm.getSendDate().compareTo(rm.getSendDate());
		}
	}

	public static int compareSendDates(@Nullable ChatMessage lm, @Nullable ChatMessage rm) {
		if(lm == null && rm == null) {
			return 0;
		} else if (lm == null) {
			return 1;
		} else if (rm == null) {
			return -1;
		} else {
			return lm.getSendDate().compareTo(rm.getSendDate());
		}
	}
}
