package org.solovyev.android.messenger.messages;

import android.text.Html;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

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
		final LocalDate sendDate = message.getSendDate().toLocalDate();
		final LocalDate today = DateTime.now().toLocalDate();
		final LocalDate yesterday = today.minusDays(1);

		if (sendDate.toDateTimeAtStartOfDay().compareTo(today.toDateTimeAtStartOfDay()) == 0) {
			// today
			// print time
			return DateTimeFormat.shortTime().print(message.getSendDate());
		} else if (sendDate.toDateTimeAtStartOfDay().compareTo(yesterday.toDateTimeAtStartOfDay()) == 0) {
			// yesterday
			// todo serso: translate
			return "Yesterday";// + ", " + DateTimeFormat.shortTime().print(sendDate);
		} else {
			// the days before yesterday
			return DateTimeFormat.shortDate().print(sendDate);
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
		return LiteChatMessageImpl.newInstance(EntityImpl.fromEntityId(messageId));
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
			return lm.getSendDate().compareTo(rm.getSendDate());
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
			return -lm.getSendDate().compareTo(rm.getSendDate());
		}
	}
}
