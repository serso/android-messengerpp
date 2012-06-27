package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface ApiUserService {

    @Nullable
    User getUserById(@NotNull Integer userId);

    @NotNull
    List<User> getUserFriends(@NotNull Integer userId);

    @NotNull
    List<User> checkOnlineUsers(@NotNull List<User> users);

}
