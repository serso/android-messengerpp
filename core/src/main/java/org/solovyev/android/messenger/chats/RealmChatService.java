package org.solovyev.android.messenger.chats;

import android.content.Context;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:29 PM
 */
public interface RealmChatService {

    @Nonnull
    List<ChatMessage> getChatMessages(@Nonnull String realmUserId, @Nonnull Context context);

    @Nonnull
    List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull Context context);

    @Nonnull
    List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull Integer offset, @Nonnull Context context);

    @Nonnull
    List<ApiChat> getUserChats(@Nonnull String realmUserId, @Nonnull Context context);

    // return: message id
    @Nonnull
    String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage chatMessage, @Nonnull Context context);
}
