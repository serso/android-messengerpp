package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface RealmUserService {

    @Nullable
    User getUserById(@NotNull String userId);

    @NotNull
    List<User> getUserContacts(@NotNull String userId);

    @NotNull
    List<User> checkOnlineUsers(@NotNull List<User> users);

}
