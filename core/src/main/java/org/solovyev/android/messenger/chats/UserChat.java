package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import org.solovyev.android.messenger.users.User;

final class UserChat {

    @Nonnull
    private final User user;

    @Nonnull
    private final Chat chat;

    UserChat(@Nonnull User user, @Nonnull Chat chat) {
        this.user = user;
        this.chat = chat;
    }

    static UserChat newInstance(@Nonnull User user, @Nonnull Chat chat) {
        return new UserChat(user, chat);
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nonnull
    public Chat getChat() {
        return chat;
    }
}
