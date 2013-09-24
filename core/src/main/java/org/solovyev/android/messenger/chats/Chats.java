package org.solovyev.android.messenger.chats;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:49 PM
 */
public final class Chats {

	private Chats() {
		throw new AssertionError();
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable ChatMessage lastMessage, @Nonnull User user, int unreadMessagesCount) {
		String result = getDisplayName(chat, lastMessage, user);
		if (unreadMessagesCount > 0) {
			result += " (" + unreadMessagesCount + ")";
		}
		return result;
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable ChatMessage message, @Nonnull User user) {
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
	public static Chat newPrivateChat(@Nonnull Entity chat) {
		return ChatImpl.newPrivate(chat);
	}

	@Nonnull
	public static ApiChat newPrivateApiChat(@Nonnull Entity chat,
											@Nonnull Collection<User> participants,
											@Nonnull Collection<ChatMessage> messages) {
		final ApiChatImpl result = ApiChatImpl.newInstance(chat, messages.size(), true);
		for (User participant : participants) {
			result.addParticipant(participant);
		}
		for (ChatMessage message : messages) {
			result.addMessage(message);
		}
		return result;
	}

	@Nonnull
	public static ApiChat newEmptyApiChat(@Nonnull Chat chat, @Nonnull List<User> participants) {
		return ApiChatImpl.newInstance(chat, Collections.<ChatMessage>emptyList(), participants);
	}
}
