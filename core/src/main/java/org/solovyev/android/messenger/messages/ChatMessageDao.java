package org.solovyev.android.messenger.messages;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.chats.ChatMessage;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatMessageDao {

    @NotNull
    List<ChatMessage> loadChatMessages(@NotNull String chatId);

    @NotNull
    MergeDaoResult<ChatMessage, Integer> mergeChatMessages(@NotNull String chatId, @NotNull List<ChatMessage> messages, boolean allowDelete, @NotNull Context context);

    @NotNull
    List<Integer> loadChatMessageIds(@NotNull String chatId);

    @NotNull
    Integer getOldestMessageForChat(@NotNull String chatId);

    @Nullable
    ChatMessage loadLastChatMessage(@NotNull String chatId);
}
