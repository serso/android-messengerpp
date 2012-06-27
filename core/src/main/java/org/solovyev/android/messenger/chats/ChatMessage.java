package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 12:56 PM
 */
// todo serso: add user id or chat id to the primary key in DAO
public interface ChatMessage extends LiteChatMessage {

    boolean isRead();

    @NotNull
    MessageDirection getDirection();

    @NotNull
    List<LiteChatMessage> getFwdMessages();

    @NotNull
    ChatMessage clone();
}
