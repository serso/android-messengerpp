package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.properties.AProperty;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface User extends MutableUserSyncData {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @NotNull
    public static final String PROPERTY_ONLINE = "online";


    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

    @NotNull
    String getId();

    @NotNull
    String getLogin();

    @NotNull
    String getDisplayName();

    @Nullable
    Gender getGender();

    boolean isOnline();

    @NotNull
    List<AProperty> getProperties();

    @NotNull
    RealmEntity getRealmUser();

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
