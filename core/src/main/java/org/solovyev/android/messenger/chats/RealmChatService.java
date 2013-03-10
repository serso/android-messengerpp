package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:29 PM
 */
public interface RealmChatService {

    @Nonnull
    List<ChatMessage> getChatMessages(@Nonnull String realmUserId);

    @Nonnull
    List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId);

    @Nonnull
    List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull Integer offset);

    @Nonnull
    List<ApiChat> getUserChats(@Nonnull String realmUserId);

    /**
     * Method sends message to the realm and, if possible, returns message is. If message id could not be returned
     * (due, for example, to the asynchronous nature of realm) - null is returned (in that case realm connection must receive message id)
     *
     * @param chat chat in which message was created
     * @param message message to be sent
     * @return message id of send message if possible
     */
    @Nullable
    String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message);

    @Nonnull
    Chat newPrivateChat(@Nonnull String realmUserId1, @Nonnull String realmUserId2);
}
