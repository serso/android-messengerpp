package org.solovyev.android.messenger.vk.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.users.ApiUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.chats.VkMessagesGetDialogsHttpTransaction;
import org.solovyev.common.utils.CollectionsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* User: serso
* Date: 5/28/12
* Time: 1:18 PM
*/
public class VkApiUserService implements ApiUserService {

    @Override
    public User getUserById(@NotNull Integer userId) {
        try {
            final List<User> users = AndroidHttpUtils.execute(VkUsersGetHttpTransaction.newInstance(userId, null));
            return CollectionsUtils.getFirstListElement(users);
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public List<User> getUserFriends(@NotNull Integer userId) {
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
}
