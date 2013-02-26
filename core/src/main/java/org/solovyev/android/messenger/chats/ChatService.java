package org.solovyev.android.messenger.chats;

import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.realms.RealmEntity;
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
    Chat updateChat(@NotNull Chat chat);

    @NotNull
    List<Chat> loadUserChats(@NotNull RealmEntity user);

    @NotNull
    MergeDaoResult<ApiChat, String> mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> chats);

    @Nullable
    Chat getChatById(@NotNull RealmEntity realmChat);

    @NotNull
    List<User> getParticipants(@NotNull RealmEntity realmChat);

    @NotNull
    List<User> getParticipantsExcept(@NotNull RealmEntity realmChat, @NotNull RealmEntity realmUser);

    @Nullable
    ChatMessage getLastMessage(@NotNull RealmEntity realmChat);

    @NotNull
    Chat createPrivateChat(@NotNull RealmEntity user, @NotNull RealmEntity secondRealmUser);

    @NotNull
    RealmEntity createPrivateChatId(@NotNull RealmEntity user, @NotNull RealmEntity secondRealmUser);

    @NotNull
    ChatMessage sendChatMessage(@NotNull RealmEntity user, @NotNull Chat chat, @NotNull ChatMessage chatMessage);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @NotNull
    List<ChatMessage> syncChatMessages(@NotNull RealmEntity user);

    @NotNull
    List<ChatMessage> syncNewerChatMessagesForChat(@NotNull RealmEntity realmChat, @NotNull RealmEntity realmUser);

    @NotNull
    List<ChatMessage> syncOlderChatMessagesForChat(@NotNull RealmEntity realmChat, @NotNull RealmEntity realmUser);

    void syncChat(@NotNull RealmEntity realmChat, @NotNull RealmEntity realmUser);

    @Nullable
    RealmEntity getSecondUser(@NotNull Chat chat);

    void setChatIcon(@NotNull ImageView imageView, @NotNull Chat chat, @NotNull User user);
}
