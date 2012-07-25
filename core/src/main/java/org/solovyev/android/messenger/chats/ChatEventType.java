package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 4:18 PM
 */
public enum ChatEventType {
    added,
    changed,

    participant_added,
    participant_removed,

    message_added,
    message_added_batch,

    // data == id of removed message
    message_removed,

    // data == changed message for chat
    message_changed,

    // data == new last message for chat
    last_message_changed,

    // data == id of user which start typing in chat
    user_start_typing;

    public boolean isEvent (@NotNull ChatEventType chatEventType, @NotNull Chat eventChat, @NotNull Chat chat) {
        return this == chatEventType && eventChat.equals(chat);
    }

}
