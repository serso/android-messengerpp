package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AProperty;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatDao {

    @NotNull
    MergeDaoResult<ApiChat, String> mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> chats);

    @NotNull
    List<String> loadUserChatIds(@NotNull String userId);

    @NotNull
    List<String> loadChatIds();

    @NotNull
    List<AProperty> loadChatPropertiesById(@NotNull String chatId);

    @NotNull
    List<Chat> loadUserChats(@NotNull String userId);

    @NotNull
    List<User> loadChatParticipants(@NotNull String chatId);

    @Nullable
    Chat loadChatById(@NotNull String chatId);

    void updateChat(@NotNull Chat chat);
}
