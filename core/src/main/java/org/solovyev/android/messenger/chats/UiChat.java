package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Chat for UI, contains additional parameters like user, last message to be shown on UI
 */
public final class UiChat implements Identifiable {

	@Nonnull
	private final User user;

	@Nonnull
	private final Chat chat;

	private final int unreadMessagesCount;

	@Nullable
	private final ChatMessage lastMessage;

	// precached display name in order to calculate it before shown (e.g. for sorting)
	@Nonnull
	private final String displayName;

	private UiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage, int unreadMessagesCount, @Nonnull String displayName) {
		this.user = user;
		this.chat = chat;
		this.lastMessage = lastMessage;
		this.unreadMessagesCount = unreadMessagesCount;
		this.displayName = displayName;
	}

	@Nonnull
	static UiChat newUiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage, int unreadMessagesCount, @Nonnull String displayName) {
		return new UiChat(user, chat, lastMessage, unreadMessagesCount, displayName);
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

	@Nonnull
	public UiChat copyForNewChat(@Nonnull Chat newChat) {
		return UiChat.newUiChat(this.user, newChat, this.lastMessage, this.unreadMessagesCount, this.displayName);
	}

	@Nonnull
	public UiChat copyForNewLastMessage(@Nonnull ChatMessage newLastMessage) {
		return UiChat.newUiChat(this.user, this.chat, newLastMessage, this.unreadMessagesCount, this.displayName);
	}

	@Nonnull
	public UiChat copyForNewUnreadMessageCount(@Nonnull Integer unreadMessagesCount) {
		return UiChat.newUiChat(this.user, this.chat, this.lastMessage, unreadMessagesCount, this.displayName);
	}
}
