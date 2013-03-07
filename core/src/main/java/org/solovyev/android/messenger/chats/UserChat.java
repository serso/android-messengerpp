package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Chat for UI, contains additional parameters like user, last message to be shown on UI
 */
final class UserChat implements MessengerEntity {

    @Nonnull
    private final User user;

    @Nonnull
    private final Chat chat;

    @Nullable
    private final ChatMessage lastMessage;

    private UserChat(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage) {
        this.user = user;
        this.chat = chat;
        this.lastMessage = lastMessage;
    }

    @Nonnull
    static UserChat newInstance(@Nonnull User user, @Nonnull Chat chat, @Nullable ChatMessage lastMessage) {
        return new UserChat(user, chat, lastMessage);
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

        final UserChat that = (UserChat) o;

        if (!chat.equals(that.chat)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return chat.hashCode();
    }

    @Nonnull
    @Override
    public String getId() {
        return this.chat.getId();
    }

    @Nonnull
    public UserChat copyForNewChat(@Nonnull Chat newChat) {
        return UserChat.newInstance(this.user, newChat, this.lastMessage);
    }

    @Nonnull
    public UserChat copyForNewLastMessage(@Nonnull ChatMessage newLastMessage) {
        return UserChat.newInstance(this.user, this.chat, newLastMessage);
    }
}
