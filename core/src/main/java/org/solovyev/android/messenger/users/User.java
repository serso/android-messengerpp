package org.solovyev.android.messenger.users;

import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AProperty;
import org.solovyev.android.VersionedEntity;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface User extends VersionedEntity<String>, MutableUserSyncData /*, Parcelable*/ {

    @NotNull
    public static final Parcelable.Creator<User> CREATOR = new UserParcelableCreator();

    @NotNull
    String getLogin();

    @NotNull
    String getDisplayName();

    @Nullable
    Gender getGender();

    boolean isOnline();

    @NotNull
    List<AProperty> getProperties();

    @Nullable
    String getPropertyValueByName(@NotNull String name);

    @NotNull
    UserSyncData getUserSyncData();

    @NotNull
    User clone();

    @NotNull
    User cloneWithNewStatus(boolean online);

    /*
    **********************************************************************
    *
    *                           UPDATE SYNC DATA
    *
    **********************************************************************
    */

    @NotNull
    User updateChatsSyncDate();

    @NotNull
    User updatePropertiesSyncDate();

    @NotNull
    User updateContactsSyncDate();

    @NotNull
    User updateUserIconsSyncDate();
}
