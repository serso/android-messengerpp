package org.solovyev.android.messenger.realms.vk.users;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* User: serso
* Date: 5/28/12
* Time: 1:18 PM
*/
public class VkRealmUserService implements RealmUserService {

    @Nonnull
    private final VkRealm realm;

    public VkRealmUserService(@Nonnull VkRealm realm) {
        this.realm = realm;
    }

    @Override
    public User getUserById(@Nonnull String realmUserId) {
        try {
            final List<User> users = HttpTransactions.execute(VkUsersGetHttpTransaction.newInstance(realm, realmUserId, null));
            return Collections.getFirstListElement(users);
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull String realmUserId) {
        try {
            return HttpTransactions.execute(VkFriendsGetHttpTransaction.newInstance(realm, realmUserId));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }


    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull List<User> users) {
        final List<User> result = new ArrayList<User>(users.size());

        try {
            for (VkUsersGetHttpTransaction vkUsersGetHttpTransaction : VkUsersGetHttpTransaction.newInstancesForUsers(realm, users, Arrays.asList(ApiUserField.online))) {
                result.addAll(HttpTransactions.execute(vkUsersGetHttpTransaction));
            }
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }

        return result;
    }
}
