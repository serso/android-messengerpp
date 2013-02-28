package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.messenger.MergeDaoResult;

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

    @NotNull
    User insertUser(@NotNull User user);

    @Nullable
    User loadUserById(@NotNull String userId);

    @NotNull
    List<AProperty> loadUserPropertiesById(@NotNull String userId);

    void updateUser(@NotNull User user);

    @NotNull
    List<String> loadUserIds();

    void deleteAllUsers();

    /*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

    @NotNull
    List<String> loadUserContactIds(@NotNull String userId);

    @NotNull
    List<User> loadUserContacts(@NotNull String userId);

    @NotNull
    MergeDaoResult<User, String> mergeUserContacts(@NotNull String userId, @NotNull List<User> contacts);

}
