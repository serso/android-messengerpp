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

    /**
     * Fired when contact presence is changed to available/online
     * Data: contact (User) - contact for which presence is changed
     */
    contact_online,

    /**
     * Fired when contact presence is changed to unavailable/offline
     * Data: contact (User) - contact for which presence is changed
     */
    contact_offline;
}
