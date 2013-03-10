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
    public ChatGuiEvent newEvent(@Nonnull Chat chat) {
        return new ChatGuiEvent(chat, this);
    }
}
