package org.solovyev.android.messenger.messages;

import android.text.Html;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.TimeZone;

import static org.joda.time.DateTime.now;
import static org.joda.time.format.DateTimeFormat.shortDate;
import static org.joda.time.format.DateTimeFormat.shortTime;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.MessageState.received;
import static org.solovyev.android.messenger.messages.MessageState.sending;
import static org.solovyev.android.messenger.messages.MessageState.sent;

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
	public static CharSequence getMessageTime(@Nonnull Message message) {
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
	public static CharSequence getMessageTitle(@Nonnull Chat chat, @Nonnull Message message, @Nonnull User user) {
		final String authorName = getMessageAuthorDisplayName(chat, message, user);
		if (Strings.isEmpty(authorName)) {
			return Html.fromHtml(message.getBody());
		} else {
			return authorName + ": " + Html.fromHtml(message.getBody());
		}
	}

	@Nonnull
	private static String getMessageAuthorDisplayName(@Nonnull Chat chat, @Nonnull Message message, @Nonnull User user) {
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
	public static MutableMessage newEmptyMessage(@Nonnull String messageId) {
		return new MessageImpl(newEntityFromEntityId(messageId));
	}

	@Nonnull
	public static MutableMessage newIncomingMessage(@Nonnull Account account, @Nonnull Chat chat, @Nonnull String message, @Nullable String title, @Nonnull Entity author) {
		final MutableMessage result = newMessage(generateEntity(account));

		result.setChat(chat.getEntity());
		result.setAuthor(author);
		if (chat.isPrivate()) {
			result.setRecipient(account.getUser().getEntity());
		}
		result.setSendDate(now());
		result.setState(received);
		result.setRead(false);
		result.setBody(message);
		result.setTitle(title == null ? "" : title);

		return result;
	}

	@Nonnull
	public static MutableMessage newOutgoingMessage(@Nonnull Account account, @Nonnull Chat chat, @Nonnull String message, @Nullable String title) {
		final MutableMessage result = newMessage(generateEntity(account));

		result.setChat(chat.getEntity());
		result.setAuthor(account.getUser().getEntity());
		if (chat.isPrivate()) {
			result.setRecipient(chat.getSecondUser());
		}
		result.setSendDate(now());
		result.setState(sending);
		result.setRead(true);
		result.setBody(message);
		result.setTitle(title == null ? "" : title);

		return result;
	}

	@Nonnull
	public static MutableMessage newMessage(@Nonnull Entity entity) {
		return new MessageImpl(entity);
	}

	public static int compareSendDatesLatestFirst(@Nullable Message lm, @Nullable Message rm) {
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

	public static int compareSendDates(@Nullable Message lm, @Nullable Message rm) {
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

	@Nonnull
	static MutableMessage copySentMessage(@Nonnull Message message, @Nonnull Account account, @Nonnull String accountMessageId) {
		final Realm realm = account.getRealm();
		final Entity messageId;
		if(accountMessageId.equals(NO_ACCOUNT_ID)) {
			// auto-generated id
			messageId = message.getEntity();
		} else {
			messageId = account.newMessageEntity(accountMessageId);
		}

		final MutableMessage result = newMessage(messageId);

		result.setChat(message.getChat());
		result.setAuthor(message.getAuthor());
		if(message.isPrivate()) {
			result.setRecipient(message.getRecipient());
		}
		result.setBody(message.getBody());
		result.setTitle(message.getTitle());
		result.setSendDate(message.getSendDate());
		if(realm.shouldWaitForDeliveryReport()) {
			result.setState(sending);
		} else {
			result.setState(sent);
		}
		result.setRead(true);
		result.getProperties().setPropertiesFrom(message.getProperties().getPropertiesCollection());

		if(!messageId.equals(message.getEntity())) {
			// id has changed => need to set original message id
			result.setOriginalId(message.getEntity().getEntityId());
		}

		return result;
	}
}
