package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public static String getDisplayName(@Nonnull Chat chat, @Nullable ChatMessage lastMessage, @Nonnull User user) {
        if (lastMessage == null) {
            return "";
        } else {
            return getChatTitle(chat, lastMessage, user);
        }
    }

    @Nonnull
    private static String getChatTitle(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
        final String title = message.getTitle();
        if (Strings.isEmpty(title) || title.equals(" ... ")) {

            if (chat.isPrivate()) {
                final User secondUser = message.getSecondUser(user);
                if (secondUser != null) {
                    return Users.getDisplayNameFor(secondUser);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return title;
        }
    }
}
