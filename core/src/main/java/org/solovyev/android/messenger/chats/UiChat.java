package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.any;
import static org.solovyev.android.messenger.App.getChatService;

/**
 * Chat for UI, contains additional parameters like user, last message to be shown on UI
 */
public final class UiChat implements Identifiable {

	@Nonnull
	private User user;

	@Nonnull
	private Chat chat;

	private int unreadMessagesCount;

	@Nullable
	private ChatMessage lastMessage;

	// precached display name in order to calculate it before shown (e.g. for sorting)
	@Nonnull
	private String displayName;

	private boolean online;

	private UiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage, int unreadMessagesCount, @Nonnull String displayName, boolean online) {
		this.user = user;
		this.chat = chat;
		this.lastMessage = lastMessage;
		this.unreadMessagesCount = unreadMessagesCount;
		this.displayName = displayName;
		this.online = online;
	}

	@Nonnull
	static UiChat newUiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage, int unreadMessagesCount, @Nonnull String displayName, boolean online) {
		return new UiChat(user, chat, lastMessage, unreadMessagesCount, displayName, online);
	}

	@Nonnull
	static UiChat loadUiChat(@Nonnull User user, @Nonnull Chat chat) {
		final ChatMessage lastMessage = getLastChatMessage(chat);
		final int unreadMessagesCount = getUnreadMessagesCount(chat);
		final String displayName = Chats.getDisplayName(chat, lastMessage, user, unreadMessagesCount);
		final boolean online = isParticipantsOnline(user, chat);

		return new UiChat(user, chat, lastMessage, unreadMessagesCount, displayName, online);
	}

	@Nonnull
	public static UiChat newEmptyUiChat(@Nonnull User user, @Nonnull Chat chat) {
		return newUiChat(user, chat, null, 0, "", false);
	}

	@Nullable
	private static ChatMessage getLastChatMessage(Chat chat) {
		return getChatService().getLastMessage(chat.getEntity());
	}

	private static int getUnreadMessagesCount(@Nonnull Chat chat) {
		return getChatService().getUnreadMessagesCount(chat.getEntity());
	}

	private static boolean isParticipantsOnline(@Nonnull User user, @Nonnull Chat chat) {
		final Iterable<User> participants = getChatService().getParticipantsExcept(chat.getEntity(), user.getEntity());

		return any(participants, new Predicate<User>() {
			@Override
			public boolean apply(User participant) {
				return participant.isOnline();
			}
		});
	}

	@Nonnull
	public User getUser() {
		return user;
	}

	@Nonnull
	public Chat getChat() {
		return chat;
	}

	@Nullable
	public ChatMessage getLastMessage() {
		return lastMessage;
	}

	public boolean isOnline() {
		return online;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final UiChat that = (UiChat) o;

		if (!chat.equals(that.chat)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return chat.hashCode();
	}

	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}

	@Nonnull
	@Override
	public String getId() {
		return this.chat.getId();
	}

	@Nonnull
	String getDisplayName() {
		return displayName;
	}


	public void setChat(@Nonnull Chat chat) {
		this.chat = chat;
	}

	public void setLastMessage(@Nullable ChatMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void setUnreadMessagesCount(int unreadMessagesCount) {
		this.unreadMessagesCount = unreadMessagesCount;
	}

	public boolean setOnline(boolean online) {
		if (this.online != online) {
			this.online = online;
			return true;
		}

		return false;
	}

	public boolean updateOnlineStatus() {
		return setOnline(isParticipantsOnline(this.user, this.chat));
	}
}
