package org.solovyev.android.messenger.chats;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.text.Strings;

import static java.lang.Math.min;
import static java.util.Collections.sort;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:49 PM
 */
public final class Chats {

	@Nonnull
	public static final String CHATS_FRAGMENT_TAG = "chats";
	static final int MAX_RECENT_CHATS = 20;

	private Chats() {
		throw new AssertionError();
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message lastMessage, @Nonnull User user, int unreadMessagesCount) {
		String result = getDisplayName(chat, lastMessage, user);
		if (unreadMessagesCount > 0) {
			result += " (" + unreadMessagesCount + ")";
		}
		return result;
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message message, @Nonnull User user) {
		final String title = message != null ? message.getTitle() : null;
		if (Strings.isEmpty(title) || title.equals(" ... ")) {

			if (chat.isPrivate()) {
				return Users.getDisplayNameFor(chat.getSecondUser());
			} else {
				return "";
			}
		} else {
			return title;
		}
	}

	@Nonnull
	public static MutableChat newChat(@Nonnull Entity entity,
							   @Nonnull List<AProperty> properties,
							   @Nullable DateTime lastMessageSyncDate) {
		return new ChatImpl(entity, properties, lastMessageSyncDate);
	}

	@Nonnull
	public static MutableChat newPrivateChat(@Nonnull Entity chat) {
		return ChatImpl.newPrivateChat(chat);
	}

	@Nonnull
	public static MutableChat newEmptyChat(@Nonnull String chatId) {
		return new ChatImpl(newEntityFromEntityId(chatId), false);
	}

	@Nonnull
	public static MutableAccountChat newAccountChat(@Nonnull Entity chat, boolean privateChat) {
		return new AccountChatImpl(chat, privateChat);
	}

	@Nonnull
	public static MutableAccountChat newPrivateAccountChat(@Nonnull Entity chat,
													@Nonnull Collection<User> participants,
													@Nonnull Collection<Message> messages) {
		final MutableAccountChat result = newAccountChat(chat, true);
		for (User participant : participants) {
			result.addParticipant(participant);
		}
		for (Message message : messages) {
			result.addMessage(message);
		}
		return result;
	}

	@Nonnull
	public static MutableAccountChat newEmptyAccountChat(@Nonnull Chat chat, @Nonnull List<User> participants) {
		return new AccountChatImpl(chat, Collections.<Message>emptyList(), participants);
	}

	@Nonnull
	static List<UiChat> getLastChatsByDate(@Nonnull List<UiChat> result, int count) {
		sort(result, new LastMessageDateChatComparator());

		return result.subList(0, min(result.size(), count));
	}
}
