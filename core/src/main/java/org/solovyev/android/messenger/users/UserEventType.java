package org.solovyev.android.messenger.users;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:22 AM
 */
public enum UserEventType {
    added,
    changed,

    contact_added,
    contact_added_batch,
    // data == id of removed contact for current user
    contact_removed,

    chat_added,
    chat_added_batch,
    // data == id of removed chat for current user
    chat_removed,

    contact_online,
    contact_offline;
}
