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

    /**
     * Method save user with his properties to persistence storage
     * Note: this method doesn't check if user with same ID is already in storage.
     *
     * @param user user to be inserted in persistence storage
     * @return newly inserted user
     */
    @Nonnull
    User insertUser(@Nonnull User user);

    /**
     * Method loads user by if from storage
     *
     * @param userId user id
     * @return user previously saved into storage identified by <var>userId</var>, null if no such user exists in storage
     */
    @Nullable
    User loadUserById(@Nonnull String userId);

    /**
     * Method loads user properties
     *
     * @param userId user id
     * @return properties of a user previously saved into storage, empty list if no such user exists in storage or no properties are set for him
     */
    @Nonnull
    List<AProperty> loadUserPropertiesById(@Nonnull String userId);

    /**
     * Method updates user and his properties in the storage
     * @param user
     */
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
    MergeDaoResult<User, String> mergeUserContacts(@Nonnull String userId,
                                                   @Nonnull List<User> contacts,
                                                   boolean allowRemoval,
                                                   boolean allowUpdate);
}
