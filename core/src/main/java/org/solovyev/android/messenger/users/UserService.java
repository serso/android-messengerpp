package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */
public interface UserService extends UserEventContainer {

    // NOTE: finding user by id always return user object, if real user cannot be found via API (e.g. user was removed) service must return dummy user object
    @NotNull
    User getUserById(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<User> getUserFriends(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<Chat> getUserChats(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    Chat getPrivateChat(@NotNull Integer userId, @NotNull Integer secondUserId, @NotNull Context context);

    @NotNull
    List<User> getOnlineUserFriends(@NotNull Integer userId, @NotNull Context context);

    void updateUser(@NotNull User user, @NotNull Context context);

    void setUserIcon(@NotNull ImageView imageView, @NotNull User user, @NotNull Context context);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    void syncUserProperties(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<User> syncUserFriends(@NotNull Integer userId, @NotNull Context context);

    @NotNull
    List<Chat> syncUserChats(@NotNull Integer userId, @NotNull Context context);

    void mergeUserChats(@NotNull Integer userId, @NotNull List<? extends ApiChat> apiChats, @NotNull Context context);

    void checkOnlineUserFriends(@NotNull Integer userId, @NotNull Context context);

    void fetchUserIcons(@NotNull User user, @NotNull Context context);
}
