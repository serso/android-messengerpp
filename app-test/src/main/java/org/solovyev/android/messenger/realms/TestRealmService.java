package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:15 PM
 */
public class TestRealmService implements RealmUserService {
    @Nullable
    @Override
    public User getUserById(@Nonnull String realmUserId) {
        return null;
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull String realmUserId) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull List<User> users) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        return Collections.emptyList();
    }
}
