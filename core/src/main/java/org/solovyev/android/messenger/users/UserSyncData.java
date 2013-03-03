package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    @Override
    UserSyncData updateChatsSyncDate();

    @Nonnull
    @Override
    UserSyncData updatePropertiesSyncDate();

    @Nonnull
    @Override
    UserSyncData updateContactsSyncDate();

    @Nonnull
    @Override
    UserSyncData updateUserIconsSyncDate();
}
