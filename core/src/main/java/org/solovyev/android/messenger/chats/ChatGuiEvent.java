package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.events.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public final class ChatGuiEvent extends AbstractTypedJEvent<Chat, ChatGuiEventType> {

    public ChatGuiEvent(@Nonnull Chat chat, @Nonnull ChatGuiEventType type) {
        super(chat, type, null);
    }

    @Nonnull
    public Chat getChat() {
        return getEventObject();
    }
}
