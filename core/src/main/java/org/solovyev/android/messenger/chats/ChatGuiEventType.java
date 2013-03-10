package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public enum ChatGuiEventType {

    chat_clicked;

    @Nonnull
    public static ChatGuiEvent newChatClicked(@Nonnull Chat chat) {
        return new ChatGuiEvent(chat, chat_clicked);
    }
}
