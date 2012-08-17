package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public enum ChatGuiEventType {

    chat_clicked;

    @NotNull
    public static ChatGuiEvent newChatClicked(@NotNull Chat chat) {
        return new ChatGuiEvent(chat_clicked, chat);
    }
}
