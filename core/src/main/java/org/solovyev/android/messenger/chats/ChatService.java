package org.solovyev.android.messenger.chats;

import android.widget.ImageView;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatService extends JEventListeners<JEventListener<ChatEvent>, ChatEvent> {

    // initial initialization: will be called once on application start
    void init();

    @Nonnull
    Chat updateChat(@Nonnull Chat chat);

    @Nonnull
    List<Chat> loadUserChats(@Nonnull Entity user);

    @Nonnull
    ApiChat saveChat(@Nonnull Entity realmUser, @Nonnull ApiChat chat);

    @Nonnull
    MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats);

    @Nullable
    Chat getChatById(@Nonnull Entity realmChat);

    @Nonnull
    List<User> getParticipants(@Nonnull Entity realmChat);

    @Nonnull
    List<User> getParticipantsExcept(@Nonnull Entity realmChat, @Nonnull Entity realmUser);

    @Nullable
    ChatMessage getLastMessage(@Nonnull Entity realmChat);

    @Nonnull
    Chat newPrivateChat(@Nonnull Entity realmUser1, @Nonnull Entity realmUser2);

    @Nonnull
    Entity newPrivateChatId(@Nonnull Entity realmUser1, @Nonnull Entity realmUser2);

    @Nonnull
    Chat getPrivateChat(@Nonnull Entity user1, @Nonnull Entity user2);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @Nonnull
    List<ChatMessage> syncNewerChatMessages(@Nonnull Entity user);

    @Nonnull
    List<ChatMessage> syncNewerChatMessagesForChat(@Nonnull Entity chat, @Nonnull Entity user);

    @Nonnull
    List<ChatMessage> syncOlderChatMessagesForChat(@Nonnull Entity chat, @Nonnull Entity user);

    void syncChat(@Nonnull Entity realmChat, @Nonnull Entity realmUser);

    @Nullable
    Entity getSecondUser(@Nonnull Chat chat);

    void setChatIcon(@Nonnull Chat chat, @Nonnull ImageView imageView);

    void saveChatMessages(@Nonnull Entity realmChat, @Nonnull List<? extends ChatMessage> messages, boolean updateChatSyncDate);
}
