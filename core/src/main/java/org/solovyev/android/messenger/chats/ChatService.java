package org.solovyev.android.messenger.chats;

import android.widget.ImageView;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.realms.RealmEntity;
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
    List<Chat> loadUserChats(@Nonnull RealmEntity user);

    @Nonnull
    ApiChat saveChat(@Nonnull RealmEntity realmUser, @Nonnull ApiChat chat);

    @Nonnull
    MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats);

    @Nullable
    Chat getChatById(@Nonnull RealmEntity realmChat);

    @Nonnull
    List<User> getParticipants(@Nonnull RealmEntity realmChat);

    @Nonnull
    List<User> getParticipantsExcept(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser);

    @Nullable
    ChatMessage getLastMessage(@Nonnull RealmEntity realmChat);

    @Nonnull
    Chat newPrivateChat(@Nonnull RealmEntity realmUser1, @Nonnull RealmEntity realmUser2);

    @Nonnull
    RealmEntity newPrivateChatId(@Nonnull RealmEntity realmUser1, @Nonnull RealmEntity realmUser2);


    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @Nonnull
    List<ChatMessage> syncChatMessages(@Nonnull RealmEntity user);

    @Nonnull
    List<ChatMessage> syncNewerChatMessagesForChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser);

    @Nonnull
    List<ChatMessage> syncOlderChatMessagesForChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser);

    void syncChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser);

    @Nullable
    RealmEntity getSecondUser(@Nonnull Chat chat);

    void setChatIcon(@Nonnull ImageView imageView, @Nonnull Chat chat, @Nonnull User user);

    void saveChatMessages(@Nonnull RealmEntity realmChat, @Nonnull List<? extends ChatMessage> messages, boolean updateChatSyncDate);
}
