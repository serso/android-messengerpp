package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                final Entity secondUser = message.getSecondUser(user.getEntity());
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
