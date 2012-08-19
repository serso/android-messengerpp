package org.solovyev.android.messenger.vk.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.AProperty;
import org.solovyev.android.APropertyImpl;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.R;
import org.solovyev.common.collections.CollectionsUtils;

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

    @Override
    public User getUserById(@NotNull String userId) {
        try {
            final List<User> users = AndroidHttpUtils.execute(VkUsersGetHttpTransaction.newInstance(userId, null));
            return CollectionsUtils.getFirstListElement(users);
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public List<User> getUserContacts(@NotNull String userId) {
        try {
            return AndroidHttpUtils.execute(VkFriendsGetHttpTransaction.newInstance(userId));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }


    @NotNull
    @Override
    public List<User> checkOnlineUsers(@NotNull List<User> users) {
        final List<User> result = new ArrayList<User>(users.size());

        try {
            for (VkUsersGetHttpTransaction vkUsersGetHttpTransaction : VkUsersGetHttpTransaction.newInstancesForUsers(users, Arrays.asList(ApiUserField.online))) {
                result.addAll(AndroidHttpUtils.execute(vkUsersGetHttpTransaction));
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }

        return result;
    }

    @NotNull
    @Override
    public List<AProperty> getUserProperties(@NotNull User user, @NotNull Context context) {
        final List<AProperty> result = new ArrayList<AProperty>(user.getProperties().size());

        for (AProperty property : user.getProperties()) {
            final String name = property.getName();
            if ( name.equals("nickName") ) {
                result.add(APropertyImpl.newInstance(context.getString(R.string.nickname), property.getValue()));
            } else if ( name.equals("sex") ) {
                result.add(APropertyImpl.newInstance(context.getString(org.solovyev.android.messenger.R.string.sex), context.getString(Gender.valueOf(property.getValue()).getCaptionResId())));
            } else if ( name.equals("bdate") ) {
                result.add(APropertyImpl.newInstance(context.getString(R.string.birth_date), property.getValue()));
            } else if ( name.equals("countryId") ) {
                result.add(APropertyImpl.newInstance(context.getString(R.string.country), property.getValue()));
            } else if ( name.equals("cityId") ) {
                result.add(APropertyImpl.newInstance(context.getString(R.string.city), property.getValue()));
            }
            
        }
        
        return result;
    }
}
