package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/19/12
 * Time: 2:33 PM
 */
public interface MutableUserSyncData {

    @Nonnull
    MutableUserSyncData updateChatsSyncDate();

    @Nonnull
    MutableUserSyncData updatePropertiesSyncDate();

    @Nonnull
    MutableUserSyncData updateContactsSyncDate();

    @Nonnull
    MutableUserSyncData updateUserIconsSyncDate();

}
