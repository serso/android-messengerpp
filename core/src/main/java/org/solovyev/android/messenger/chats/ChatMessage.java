package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.messages.LiteChatMessage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 12:56 PM
 */
public interface ChatMessage extends LiteChatMessage {

    boolean isRead();

    @Nonnull
    MessageDirection getDirection();

    @Nonnull
    List<LiteChatMessage> getFwdMessages();

    @Nonnull
    ChatMessage clone();
}
