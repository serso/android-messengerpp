package org.solovyev.android.messenger.users;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:22 AM
 */
public enum UserEventType {
    added,
    changed,

    friend_added,
    friend_added_batch,
    friend_removed,

    chat_added,
    chat_added_batch,
    chat_removed,

    friend_online,
    friend_offline;
}
