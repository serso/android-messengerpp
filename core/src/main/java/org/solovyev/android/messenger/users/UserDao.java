package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */
public interface UserDao {

    /*
    **********************************************************************
    *
    *                           USERS
    *
    **********************************************************************
    */

    @Nonnull
    User insertUser(@Nonnull User user);

    @Nullable
    User loadUserById(@Nonnull String userId);

    @Nonnull
    List<AProperty> loadUserPropertiesById(@Nonnull String userId);

    void updateUser(@Nonnull User user);

    @Nonnull
    List<String> loadUserIds();

    void deleteAllUsers();

    void deleteAllUsersInRealm(@Nonnull String realmId);

    /*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

    @Nonnull
    List<String> loadUserContactIds(@Nonnull String userId);

    @Nonnull
    List<User> loadUserContacts(@Nonnull String userId);

    @Nonnull
    MergeDaoResult<User, String> mergeUserContacts(@Nonnull String userId, @Nonnull List<User> contacts, boolean allowRemoval);
}
