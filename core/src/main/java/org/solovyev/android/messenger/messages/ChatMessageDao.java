package org.solovyev.android.messenger.messages;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.chats.ChatMessage;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatMessageDao {

    @Nonnull
    List<ChatMessage> loadChatMessages(@Nonnull String chatId);

    @Nonnull
    MergeDaoResult<ChatMessage, String> mergeChatMessages(@Nonnull String chatId, @Nonnull List<ChatMessage> messages, boolean allowDelete, @Nonnull Context context);

    @Nonnull
    List<String> loadChatMessageIds(@Nonnull String chatId);

    @Nonnull
    String getOldestMessageForChat(@Nonnull String chatId);

    @Nullable
    ChatMessage loadLastChatMessage(@Nonnull String chatId);
}
