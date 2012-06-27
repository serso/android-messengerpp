package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AProperty;
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
    User loadUserById(@NotNull Integer userId);

    @NotNull
    List<AProperty> loadUserPropertiesById(@NotNull Integer userId);

    void updateUser(@NotNull User user);

    @NotNull
    List<Integer> loadUserIds();

    /*
    **********************************************************************
    *
    *                           FRIENDS
    *
    **********************************************************************
    */

    @NotNull
    List<Integer> loadUserFriendIds(@NotNull Integer userId);

    @NotNull
    List<User> loadUserFriends(@NotNull Integer userId);

    @NotNull
    MergeDaoResult<User, Integer> mergeUserFriends(@NotNull Integer userId, @NotNull List<User> friends);

}
