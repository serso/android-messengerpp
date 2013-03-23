package org.solovyev.android.messenger.chats;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public final class ChatGuiEvent extends AbstractTypedJEvent<Chat, ChatGuiEventType> {

    public ChatGuiEvent(@Nonnull Chat chat, @Nonnull ChatGuiEventType type, @Nullable Object data) {
        super(chat, type, data);
    }

    @Nonnull
    public Chat getChat() {
        return getEventObject();
    }

    @Nonnull
    public ChatMessage getDataAsChatMessage() {
        return (ChatMessage) getData();
    }
}
