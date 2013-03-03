package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public class ChatGuiEvent {

    @Nonnull
    private final ChatGuiEventType type;

    @Nonnull
    private final Chat chat;

    public ChatGuiEvent(@Nonnull ChatGuiEventType type,
                        @Nonnull Chat chat) {
        this.chat = chat;
        this.type = type;
    }

    @Nonnull
    public ChatGuiEventType getType() {
        return type;
    }

    @Nonnull
    public Chat getChat() {
        return chat;
    }
}
