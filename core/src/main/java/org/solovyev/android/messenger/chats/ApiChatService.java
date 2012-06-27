package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:29 PM
 */
public interface ApiChatService {

    @NotNull
    List<ChatMessage> getChatMessages(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<ChatMessage> getNewerChatMessagesForChat(@NotNull String chatId, @NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<ChatMessage> getOlderChatMessagesForChat(@NotNull String chatId, @NotNull Integer userId, @NotNull Integer offset, @NotNull Context context);

    @NotNull
    List<ApiChat> getUserChats(@NotNull Integer userId, @NotNull Context context);

    // return: message id
    @NotNull
    Integer sendChatMessage(@NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context);
}
