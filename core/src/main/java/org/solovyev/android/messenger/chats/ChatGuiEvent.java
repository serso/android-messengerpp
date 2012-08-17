package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public class ChatGuiEvent {

    @NotNull
    private final ChatGuiEventType type;

    @NotNull
    private final Chat chat;

    public ChatGuiEvent(@NotNull ChatGuiEventType type,
                        @NotNull Chat chat) {
        this.chat = chat;
        this.type = type;
    }

    @NotNull
    public ChatGuiEventType getType() {
        return type;
    }

    @NotNull
    public Chat getChat() {
        return chat;
    }
}
