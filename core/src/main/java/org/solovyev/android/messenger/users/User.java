package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.realms.RealmUser;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.VersionedEntity;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface User extends VersionedEntity<String>, MutableUserSyncData {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @NotNull
    public static final String PROPERTY_ONLINE = "online";

    @NotNull
    public static final String FAKE_REALM_ID = "fake";


    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

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
    RealmUser getRealmUser();

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
