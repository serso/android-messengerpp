package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.icons.UserIconService;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */
/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface UserService extends UserIconService {

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
    Chat getPrivateChat(@Nonnull RealmEntity realmUser1, @Nonnull RealmEntity realmUser2);

    @Nonnull
    List<User> getOnlineUserContacts(@Nonnull RealmEntity realmUser);

    void updateUser(@Nonnull User user);

    void removeUsersInRealm(@Nonnull String realmId);

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

    void mergeUserContacts(@Nonnull RealmEntity realmUser, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate);

    void checkOnlineUserContacts(@Nonnull RealmEntity realmUser);

    void fetchUserIcons(@Nonnull User user);

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    /**
     * Method subscribes listener for user events notifications
     *
     * @param listener listener to be subscribed
     * @return true if was added, false if listener already exists
     */
    boolean addListener(@Nonnull JEventListener<UserEvent> listener);

    /**
     * Method unsubscribes listener from user events notifications
     *
     * @param listener listener to be unsubscribed
     * @return true if listener was successfully unsubscribed, false if no such listener was found
     */
    boolean removeListener(@Nonnull JEventListener<UserEvent> listener);

}
