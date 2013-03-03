package org.solovyev.android.messenger.users;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import javax.annotation.Nonnull;
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
    @Nonnull
    User getUserById(@Nonnull RealmEntity realmUser);

    @Nonnull
    User getUserById(@Nonnull RealmEntity realmUser, boolean tryFindInRealm);

    @Nonnull
    List<User> getUserContacts(@Nonnull RealmEntity realmUser);

    @Nonnull
    List<Chat> getUserChats(@Nonnull RealmEntity realmUser);

    @Nonnull
    Chat getPrivateChat(@Nonnull RealmEntity realmUser, @Nonnull RealmEntity secondRealmUser);

    @Nonnull
    List<User> getOnlineUserContacts(@Nonnull RealmEntity realmUser);

    void updateUser(@Nonnull User user);

    void removeUsersInRealm(@Nonnull String realmId);


    /*
    **********************************************************************
    *
    *                           ICONS/PHOTOS
    *
    **********************************************************************
    */
    @Nonnull
    Drawable getDefaultUserIcon();

    void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

    void setUserIcon(@Nonnull User user, @Nonnull OnImageLoadedListener imageLoadedListener);

    void setUserPhoto(@Nonnull ImageView imageView, @Nonnull User user);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    void syncUserProperties(@Nonnull RealmEntity realmUser);

    @Nonnull
    List<User> syncUserContacts(@Nonnull RealmEntity realmUser);

    @Nonnull
    List<Chat> syncUserChats(@Nonnull RealmEntity realmUser);

    void mergeUserChats(@Nonnull RealmEntity realmUser, @Nonnull List<? extends ApiChat> apiChats);

    void checkOnlineUserContacts(@Nonnull RealmEntity realmUser);

    void fetchUserIcons(@Nonnull User user);
}
