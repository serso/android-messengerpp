package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AProperty;

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

    /**
     * Return list of translated user properties where property name = title, property value = value
     * @param user user which properties will be returned
     * @return list of translated user properties
     */
    @NotNull
    List<AProperty> getUserProperties(@NotNull User user, @NotNull Context context);

}
