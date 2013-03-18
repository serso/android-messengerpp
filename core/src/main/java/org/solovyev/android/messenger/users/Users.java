package org.solovyev.android.messenger.users;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:17 PM
 */
public final class Users {

    private Users() {
    }

    @Nonnull
    public static String getDisplayNameFor(@Nonnull Entity user) {
        return MessengerApplication.getServiceLocator().getUserService().getUserById(user).getDisplayName();
    }

    @Nonnull
    public static User newUser(@Nonnull String reamId,
                               @Nonnull String realmUserId,
                               @Nonnull UserSyncData userSyncData,
                               @Nonnull List<AProperty> properties) {
        final Entity entity = EntityImpl.newInstance(reamId, realmUserId);
        return newUser(entity, userSyncData, properties);
    }

    @Nonnull
    public static User newEmptyUser(@Nonnull Entity realmUser) {
        return newUser(realmUser, Users.newNeverSyncedUserSyncData(), Collections.<AProperty>emptyList());
    }

    @Nonnull
    public static User newEmptyUser(@Nonnull String userId) {
        return newEmptyUser(EntityImpl.fromEntityId(userId));
    }

    @Nonnull
    public static User newUser(@Nonnull Entity entity,
                               @Nonnull UserSyncData userSyncData,
                               @Nonnull List<AProperty> properties) {
        return UserImpl.newInstance(entity, userSyncData, properties);
    }

    @Nonnull
    public static UserSyncData newNeverSyncedUserSyncData() {
        return UserSyncDataImpl.newNeverSyncedInstance();
    }

    @Nonnull
    public static UserSyncData newUserSyncData(@Nullable DateTime lastPropertiesSyncDate,
                                                    @Nullable DateTime lastContactsSyncDate,
                                                    @Nullable DateTime lastChatsSyncDate,
                                                    @Nullable DateTime lastUserIconsSyncDate) {
        return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
    }

    @Nonnull
    public static UserSyncData newUserSyncData(@Nullable String lastPropertiesSyncDate,
                                               @Nullable String lastContactsSyncDate,
                                               @Nullable String lastChatsSyncDate,
                                               @Nullable String lastUserIconsSyncDate) {
        return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
    }
}
