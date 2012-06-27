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
public interface UserSyncData extends JCloneable<UserSyncData> {

    @Nullable
    DateTime getLastPropertiesSyncDate();

    @Nullable
    DateTime getLastFriendsSyncDate();

    @Nullable
    DateTime getLastChatsSyncDate();

    @Nullable
    DateTime getLastUserIconsSyncData();

    @NotNull
    UserSyncData updateChatsSyncDate();

    @NotNull
    UserSyncData updatePropertiesSyncDate();

    @NotNull
    UserSyncData updateFriendsSyncDate();

    @NotNull
    UserSyncData updateUserIconsSyncData();
}
