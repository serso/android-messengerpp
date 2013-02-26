package org.solovyev.android.messenger.users;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.OnImageLoadedListener;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.realms.RealmEntity;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */
public interface UserService extends UserEventListeners {

    // initial initialization: will be called once on application start
    void init();

    // NOTE: finding user by id always return user object, if real user cannot be found via API (e.g. user was removed) service must return dummy user object
    @NotNull
    User getUserById(@NotNull RealmEntity realmUser);

    @NotNull
    List<User> getUserContacts(@NotNull RealmEntity realmUser);

    @NotNull
    List<Chat> getUserChats(@NotNull RealmEntity realmUser);

    @NotNull
    Chat getPrivateChat(@NotNull RealmEntity realmUser, @NotNull RealmEntity secondRealmUser);

    @NotNull
    List<User> getOnlineUserContacts(@NotNull RealmEntity realmUser);

    void updateUser(@NotNull User user);


    /*
    **********************************************************************
    *
    *                           ICONS/PHOTOS
    *
    **********************************************************************
    */
    @NotNull
    Drawable getDefaultUserIcon();

    void setUserIcon(@NotNull User user, @NotNull ImageView imageView);

    void setUserIcon(@NotNull User user, @NotNull OnImageLoadedListener imageLoadedListener);

    void setUserPhoto(@NotNull ImageView imageView, @NotNull User user);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    void syncUserProperties(@NotNull RealmEntity realmUser);

    @NotNull
    List<User> syncUserContacts(@NotNull RealmEntity realmUser);

    @NotNull
    List<Chat> syncUserChats(@NotNull RealmEntity realmUser);

    void mergeUserChats(@NotNull RealmEntity realmUser, @NotNull List<? extends ApiChat> apiChats);

    void checkOnlineUserContacts(@NotNull RealmEntity realmUser);

    void fetchUserIcons(@NotNull User user);
}
