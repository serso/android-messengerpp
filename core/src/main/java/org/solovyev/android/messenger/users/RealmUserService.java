package org.solovyev.android.messenger.users;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.properties.AProperty;

import java.util.List;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface RealmUserService {

    @Nullable
    User getUserById(@Nonnull String realmUserId);

    @Nonnull
    List<User> getUserContacts(@Nonnull String realmUserId);

    @Nonnull
    List<User> checkOnlineUsers(@Nonnull List<User> users);

    /**
     * Return list of translated user properties where property name = title, property value = value
     * @param user user which properties will be returned
     * @return list of translated user properties
     */
    @Nonnull
    List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context);

}
