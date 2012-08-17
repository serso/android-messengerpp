package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.common.JCloneable;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:43 PM
 */
public interface UserSyncData extends MutableUserSyncData, JCloneable<UserSyncData> {

    boolean isFirstSyncDone();

    @Nullable
    DateTime getLastPropertiesSyncDate();

    @Nullable
    DateTime getLastContactsSyncDate();

    @Nullable
    DateTime getLastChatsSyncDate();

    @Nullable
    DateTime getLastUserIconsSyncData();

    /*
    **********************************************************************
    *
    *                           UPDATE
    *
    **********************************************************************
    */

    @NotNull
    @Override
    UserSyncData updateChatsSyncDate();

    @NotNull
    @Override
    UserSyncData updatePropertiesSyncDate();

    @NotNull
    @Override
    UserSyncData updateContactsSyncDate();

    @NotNull
    @Override
    UserSyncData updateUserIconsSyncDate();
}
