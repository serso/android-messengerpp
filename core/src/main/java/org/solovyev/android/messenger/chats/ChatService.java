package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatService extends ChatEventContainer {

    // initial initialization: will be called once on application start
    void init();

    @NotNull
    Chat updateChat(@NotNull Chat chat, @NotNull Context context);

    @NotNull
    List<Chat> loadUserChats(@NotNull String userId, @NotNull Context context);

    @NotNull
    MergeDaoResult<ApiChat, String> mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> chats, @NotNull Context context);

    @Nullable
    Chat getChatById(@NotNull String chatId, @NotNull Context context);

    @NotNull
    List<User> getParticipants(@NotNull String chatId, @NotNull Context context);

    @NotNull
    List<User> getParticipantsExcept(@NotNull String chatId, @NotNull String userId, @NotNull Context context);

    @Nullable
    ChatMessage getLastMessage(@NotNull String chatId, @NotNull Context context);

    @NotNull
    Chat createPrivateChat(@NotNull String userId, @NotNull String secondUserId, @NotNull Context context);

    @NotNull
    String createPrivateChatId(@NotNull String userId, @NotNull String secondUserId);

    @NotNull
    ChatMessage sendChatMessage(@NotNull String userId, @NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @NotNull
    List<ChatMessage> syncChatMessages(@NotNull String userId, @NotNull Context context);

    @NotNull
    List<ChatMessage> syncNewerChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context);

    @NotNull
    List<ChatMessage> syncOlderChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context);

    void syncChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context);

    @Nullable
    String getSecondUserId(@NotNull Chat chat);

    void setChatIcon(@NotNull ImageView imageView, @NotNull Chat chat, @NotNull User user, @NotNull Context context);
}
