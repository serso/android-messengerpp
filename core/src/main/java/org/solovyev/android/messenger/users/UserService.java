package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.icons.UserIconService;
import org.solovyev.android.messenger.entities.Entity;
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
    User getUserById(@Nonnull Entity realmUser);

    @Nonnull
    User getUserById(@Nonnull Entity realmUser, boolean tryFindInRealm);

    @Nonnull
    List<Chat> getUserChats(@Nonnull Entity realmUser);

    @Nonnull
    Chat getPrivateChat(@Nonnull Entity realmUser1, @Nonnull Entity realmUser2);

    void updateUser(@Nonnull User user);

    void removeUsersInRealm(@Nonnull String realmId);

    /*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

    /**
     * @param user user
     * @return list of all user contacts
     */
    @Nonnull
    List<User> getUserContacts(@Nonnull Entity user);

    /**
     * NOTE: method do not check real status of user on the current moment of time but get one from the cache => it might be different
     *
     * @param user user
     * @return list of all online user contacts
     */
    @Nonnull
    List<User> getOnlineUserContacts(@Nonnull Entity user);

    /**
     * Call this method when presence of user's contact has been changed.
     *
     * @param user user
     * @param contact user's contact which presence has been changed
     * @param available new presence value
     */
    void onContactPresenceChanged(@Nonnull User user, @Nonnull User contact, boolean available);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    void syncUserProperties(@Nonnull Entity realmUser);

    @Nonnull
    List<User> syncUserContacts(@Nonnull Entity realmUser);

    @Nonnull
    List<Chat> syncUserChats(@Nonnull Entity realmUser);

    void mergeUserChats(@Nonnull Entity realmUser, @Nonnull List<? extends ApiChat> apiChats);

    void mergeUserContacts(@Nonnull Entity realmUser, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate);

    void checkOnlineUserContacts(@Nonnull Entity realmUser);

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
