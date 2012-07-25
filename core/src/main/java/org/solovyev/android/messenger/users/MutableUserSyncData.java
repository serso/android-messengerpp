package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 7/19/12
 * Time: 2:33 PM
 */
public interface MutableUserSyncData {

    @NotNull
    MutableUserSyncData updateChatsSyncDate();

    @NotNull
    MutableUserSyncData updatePropertiesSyncDate();

    @NotNull
    MutableUserSyncData updateContactsSyncDate();

    @NotNull
    MutableUserSyncData updateUserIconsSyncDate();

}
