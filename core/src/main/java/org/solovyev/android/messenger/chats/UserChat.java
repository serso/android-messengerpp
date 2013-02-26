package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;

final class UserChat {

    @NotNull
    private final User user;

    @NotNull
    private final Chat chat;

    UserChat(@NotNull User user, @NotNull Chat chat) {
        this.user = user;
        this.chat = chat;
    }

    static UserChat newInstance(@NotNull User user, @NotNull Chat chat) {
        return new UserChat(user, chat);
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public Chat getChat() {
        return chat;
    }
}
