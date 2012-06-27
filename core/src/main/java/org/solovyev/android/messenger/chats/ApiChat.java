package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:10 PM
 */
public interface ApiChat {

    @NotNull
    List<ChatMessage> getMessages();

    @Nullable
    ChatMessage getLastMessage();

    @NotNull
    List<User> getParticipants();

    @NotNull
    List<User> getParticipantsExcept(@NotNull User user);

    @NotNull
    Chat getChat();
}
