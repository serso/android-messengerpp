package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 12:56 PM
 */
// todo serso: add user id or chat id to the primary key in DAO
public interface ChatMessage extends LiteChatMessage {

    boolean isRead();

    @Nonnull
    MessageDirection getDirection();

    @Nonnull
    List<LiteChatMessage> getFwdMessages();

    @Nonnull
    ChatMessage clone();
}
