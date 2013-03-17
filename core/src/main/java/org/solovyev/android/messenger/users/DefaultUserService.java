package org.solovyev.android.messenger.users;

import android.app.Application;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmMapEntryMatcher;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */

@Singleton
public class DefaultUserService implements UserService {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */
    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private UserDao userDao;

    @Inject
    @Nonnull
    private Application context;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    /**
     * Lock for all operations with persistence state. Should guarantee that all operations done over DAOs are thread safe and not corrupt data.
     */
    @Nonnull
    private final Object lock = new Object();

    @Nonnull
    private final JEventListeners<JEventListener<? extends UserEvent>, UserEvent> listeners = Listeners.newEventListenersBuilderFor(UserEvent.class).withHardReferences().onCallerThread().create();

    // key: user entity, value: list of user contacts
    @GuardedBy("userContactsCache")
    @Nonnull
    private final Map<Entity, List<User>> userContactsCache = new HashMap<Entity, List<User>>();

    // key: user entity, value: list of user chats
    @GuardedBy("userChatsCache")
    @Nonnull
    private final Map<Entity, List<Chat>> userChatsCache = new HashMap<Entity, List<Chat>>();

    // key: user entity, value: user object
    @GuardedBy("usersCache")
    @Nonnull
    private final Map<Entity, User> usersCache = new HashMap<Entity, User>();

    public DefaultUserService() {
        listeners.addListener(new UserEventListener());
    }

    @Override
    public void init() {
        chatService.addListener(new ChatEventListener());
    }

    @Nonnull
    @Override
    public User getUserById(@Nonnull Entity user) {
        return getUserById(user, true);
    }

    @Nonnull
    @Override
    public User getUserById(@Nonnull Entity user, boolean tryFindInRealm) {
        boolean saved = true;

        User result;

        synchronized (usersCache) {
            result = usersCache.get(user);
        }

        if (result == null) {

            synchronized (lock) {
                result = userDao.loadUserById(user.getEntityId());
            }

            if (result == null) {
                saved = false;
            }

            if (result == null) {
                if (tryFindInRealm) {
                    final Realm realm = getRealmByUser(user);
                    result = realm.getRealmUserService().getUserById(user.getRealmEntityId());
                }
            }

            if (result == null) {
                result = Users.newEmptyUser(user);
            } else {
                // user was loaded either from dao or from API => cache
                synchronized (usersCache) {
                    usersCache.put(user, result);
                }
            }

            if (!saved) {
                insertUser(result);
            }
        }

        return result;
    }

    @Nonnull
    private Realm getRealmByUser(@Nonnull Entity realmUser) {
        return realmService.getRealmById(realmUser.getRealmId());
    }

    private void insertUser(@Nonnull User user) {
        boolean inserted = false;

        synchronized (lock) {
            final User userFromDb = userDao.loadUserById(user.getEntity().getEntityId());
            if (userFromDb == null) {
                userDao.insertUser(user);
                inserted = true;
            }
        }

        if (inserted) {
            listeners.fireEvent(UserEventType.added.newEvent(user));
        }
    }

    @Nonnull
    @Override
    public List<Chat> getUserChats(@Nonnull Entity user) {
        List<Chat> result;

        synchronized (userChatsCache) {
            result = userChatsCache.get(user);
            if (result == null) {
                result = chatService.loadUserChats(user);
                if (!Collections.isEmpty(result)) {
                    userChatsCache.put(user, result);
                }
            }
        }

        // result list might be in cache and might updates due to some user events => must COPY
        return new ArrayList<Chat>(result);
    }

    @Override
    public void updateUser(@Nonnull User user) {
        updateUser(user, true);
    }

    private void updateUser(@Nonnull User user, boolean fireChangeEvent) {
        synchronized (lock) {
            userDao.updateUser(user);
        }

        if (fireChangeEvent) {
            listeners.fireEvent(UserEventType.changed.newEvent(user));
        }
    }

    @Override
    public void removeUsersInRealm(@Nonnull final String realmId) {
        synchronized (lock) {
            this.userDao.deleteAllUsersInRealm(realmId);
        }

        synchronized (userChatsCache) {
            Iterators.removeIf(userChatsCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));
        }

        synchronized (userContactsCache) {
            Iterators.removeIf(userContactsCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));
        }

        synchronized (usersCache) {
            Iterators.removeIf(usersCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));
        }
    }

    /*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull Entity user) {
        List<User> result;

        synchronized (userContactsCache) {
            result = userContactsCache.get(user);
            if (result == null) {
                synchronized (lock) {
                    result = userDao.loadUserContacts(user.getEntityId());
                }
                if (!Collections.isEmpty(result)) {
                    userContactsCache.put(user, result);
                }
            }

            // result list might be in cache and might updates due to some user events => must COPY
            return new ArrayList<User>(result);
        }
    }

    @Nonnull
    @Override
    public List<User> getOnlineUserContacts(@Nonnull Entity user) {
        return Lists.newArrayList(Iterables.filter(getUserContacts(user), new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User contact) {
                return contact != null && contact.isOnline();
            }
        }));
    }

    @Override
    public void onContactPresenceChanged(@Nonnull User user, @Nonnull final User contact, boolean available) {
        final UserEventType userEventType = available ? UserEventType.contact_online : UserEventType.contact_offline;
        // update cache
        synchronized (userContactsCache) {
            final List<User> contacts = userContactsCache.get(user.getEntity());
            final int index = Iterables.indexOf(contacts, new Predicate<User>() {
                @Override
                public boolean apply(@Nullable User user) {
                    return contact.equals(user);
                }
            });
            if ( index >= 0 ) {
                // contact found => update status locally (persistence is not updated at status change is too frequent event)
                contacts.set(index, contacts.get(index).cloneWithNewStatus(available));
            }
        }
        this.listeners.fireEvent(userEventType.newEvent(user, contact));
    }

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @Override
    public void syncUserProperties(@Nonnull Entity entityUser) {
        final User user = getRealmByUser(entityUser).getRealmUserService().getUserById(entityUser.getRealmEntityId());
        if (user != null) {
            updateUser(user, true);
        }
    }

    @Override
    @Nonnull
    public List<User> syncUserContacts(@Nonnull Entity user) {
        final List<User> contacts = getRealmByUser(user).getRealmUserService().getUserContacts(user.getRealmEntityId());
        synchronized (userContactsCache) {
            userContactsCache.put(user, contacts);
        }

        mergeUserContacts(user, contacts, false, true);

        return java.util.Collections.unmodifiableList(contacts);
    }

    @Override
    public void mergeUserContacts(@Nonnull Entity entityUser, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
        User user = getUserById(entityUser);
        final MergeDaoResult<User, String> result;
        synchronized (lock) {
            result = userDao.mergeUserContacts(entityUser.getEntityId(), contacts, allowRemoval, allowUpdate);

            // update sync data
            user = user.updateContactsSyncDate();
            updateUser(user, false);
        }

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        userEvents.add(UserEventType.contact_added_batch.newEvent(user, result.getAddedObjectLinks()));

        final List<User> addedContacts = result.getAddedObjects();
        for (User addedContact : addedContacts) {
            userEvents.add(UserEventType.added.newEvent(addedContact));
        }
        userEvents.add(UserEventType.contact_added_batch.newEvent(user, addedContacts));


        for (String removedContactId : result.getRemovedObjectIds()) {
            userEvents.add(UserEventType.contact_removed.newEvent(user, removedContactId));
        }

        for (User updatedContact : result.getUpdatedObjects()) {
            userEvents.add(UserEventType.changed.newEvent(updatedContact));
            final UserEventType type = updatedContact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline;
            userEvents.add(type.newEvent(user, updatedContact));
        }

        listeners.fireEvents(userEvents);
    }

    @Nonnull
    @Override
    public List<Chat> syncUserChats(@Nonnull Entity user) {
        final List<ApiChat> apiChats = getRealmByUser(user).getRealmChatService().getUserChats(user.getRealmEntityId());

        final List<Chat> chats = Lists.newArrayList(Iterables.transform(apiChats, new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat input) {
                assert input != null;
                return input.getChat();
            }
        }));

        mergeUserChats(user, apiChats);

        return java.util.Collections.unmodifiableList(chats);
    }

    @Override
    public void mergeUserChats(@Nonnull Entity userEntity, @Nonnull List<? extends ApiChat> apiChats) {
        User user = this.getUserById(userEntity);

        final MergeDaoResult<ApiChat, String> result;
        synchronized (lock) {
            result = chatService.mergeUserChats(userEntity.getEntityId(), apiChats);

            // update sync data
            user = user.updateChatsSyncDate();
            updateUser(user, false);
        }

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(apiChats.size());
        final List<ChatEvent> chatEvents = new ArrayList<ChatEvent>(apiChats.size());

        final List<Chat> addedChatLinks = Lists.transform(result.getAddedObjectLinks(), new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
                assert apiChat != null;
                return apiChat.getChat();
            }
        });

        if (!addedChatLinks.isEmpty()) {
            userEvents.add(UserEventType.chat_added_batch.newEvent(user, addedChatLinks));
        }

        final List<Chat> addedChats = Lists.transform(result.getAddedObjects(), new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
                assert apiChat != null;
                return apiChat.getChat();
            }
        });

        for (Chat addedChat : addedChats) {
            chatEvents.add(ChatEventType.added.newEvent(addedChat));
        }
        if (!addedChats.isEmpty()) {
            userEvents.add(UserEventType.chat_added_batch.newEvent(user, addedChats));
        }

        for (String removedChatId : result.getRemovedObjectIds()) {
            userEvents.add(UserEventType.chat_removed.newEvent(user, removedChatId));
        }

        for (ApiChat updatedChat : result.getUpdatedObjects()) {
            chatEvents.add(ChatEventType.changed.newEvent(updatedChat.getChat()));
        }

        listeners.fireEvents(userEvents);
        chatService.fireEvents(chatEvents);
    }

    @Override
    public void checkOnlineUserContacts(@Nonnull Entity userEntity) {
        final List<User> contacts = getRealmByUser(userEntity).getRealmUserService().checkOnlineUsers(getUserContacts(userEntity));

        final User user = getUserById(userEntity);

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        for (User contact : contacts) {
            final UserEventType type = contact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline;
            userEvents.add(type.newEvent(user, contact));
        }

        listeners.fireEvents(userEvents);
    }

    /*
    **********************************************************************
    *
    *                           USER ICONS
    *
    **********************************************************************
    */

    @Override
    public void fetchUserAndContactsIcons(@Nonnull User user) {
        final RealmIconService realmIconService = getRealmIconServiceByUser(user);

        // fetch self icon
        realmIconService.fetchUsersIcons(Arrays.asList(user));

        // fetch icons for all contacts
        final List<User> contacts = getUserContacts(user.getEntity());
        realmIconService.fetchUsersIcons(contacts);

        // update sync data
        user = user.updateUserIconsSyncDate();
        updateUser(user, false);
    }

    @Nonnull
    private RealmIconService getRealmIconServiceByUser(@Nonnull User user) {
        return getRealmByUser(user.getEntity()).getRealmDef().getRealmIconService();
    }

    @Override
    public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
        getRealmIconServiceByUser(user).setUserIcon(user, imageView);
    }

    @Override
    public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
        getRealmIconServiceByUser(user).setUserPhoto(user, imageView);
    }

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    @Override
    public boolean addListener(@Nonnull JEventListener<UserEvent> listener) {
        return this.listeners.addListener(listener);
    }

    @Override
    public boolean removeListener(@Nonnull JEventListener<UserEvent> listener) {
        return this.listeners.removeListener(listener);
    }

    private final class UserEventListener extends AbstractJEventListener<UserEvent> {

        private UserEventListener() {
            super(UserEvent.class);
        }

        @Override
        public void onEvent(@Nonnull UserEvent event) {
            final UserEventType type = event.getType();
            final User eventUser = event.getUser();

            synchronized (userContactsCache) {

                if (type == UserEventType.changed) {
                    // user changed => update it in contacts cache
                    for (List<User> contacts : userContactsCache.values()) {
                        for (int i = 0; i < contacts.size(); i++) {
                            final User contact = contacts.get(i);
                            if (contact.equals(eventUser)) {
                                contacts.set(i, eventUser);
                            }
                        }
                    }
                }

                if (type == UserEventType.contact_added) {
                    // contact added => need to add to list of cached contacts
                    final User contact = event.getDataAsUser();
                    final List<User> contacts = userContactsCache.get(eventUser.getEntity());
                    if (contacts != null) {
                        // check if not contains as can be added in parallel
                        if (!Iterables.contains(contacts, contact)) {
                            contacts.add(contact);
                        }
                    }
                }

                if (type == UserEventType.contact_added_batch) {
                    // contacts added => need to add to list of cached contacts
                    final List<User> contacts = event.getDataAsUsers();
                    final List<User> contactsFromCache = userContactsCache.get(eventUser.getEntity());
                    if (contactsFromCache != null) {
                        for (User contact : contacts) {
                            // check if not contains as can be added in parallel
                            if (!Iterables.contains(contactsFromCache, contact)) {
                                contactsFromCache.add(contact);
                            }
                        }
                    }
                }

                if (type == UserEventType.contact_removed) {
                    // contact removed => try to remove from cached contacts
                    final String removedContactId = event.getDataAsUserId();
                    final List<User> contacts = userContactsCache.get(eventUser.getEntity());
                    if (contacts != null) {
                        Iterables.removeIf(contacts, new Predicate<User>() {
                            @Override
                            public boolean apply(@javax.annotation.Nullable User contact) {
                                return contact != null && contact.getEntity().getEntityId().equals(removedContactId);
                            }
                        });
                    }
                }
            }

            synchronized (userChatsCache) {
                if (type == UserEventType.chat_added) {
                    final Chat chat = event.getDataAsChat();
                    final List<Chat> chats = userChatsCache.get(eventUser.getEntity());
                    if (chats != null) {
                        if (!Iterables.contains(chats, chat)) {
                            chats.add(chat);
                        }
                    }
                }

                if (type == UserEventType.chat_added_batch) {
                    final List<Chat> chats = event.getDataAsChats();
                    final List<Chat> chatsFromCache = userChatsCache.get(eventUser.getEntity());
                    if (chatsFromCache != null) {
                        for (Chat chat : chats) {
                            if (!Iterables.contains(chatsFromCache, chat)) {
                                chatsFromCache.add(chat);
                            }
                        }
                    }
                }

                if (type == UserEventType.chat_removed) {
                    final Chat chat = event.getDataAsChat();
                    final List<Chat> chats = userChatsCache.get(eventUser.getEntity());
                    if (chats != null) {
                        chats.remove(chat);
                    }
                }
            }

            synchronized (usersCache) {
                if (type == UserEventType.changed) {
                    usersCache.put(eventUser.getEntity(), eventUser);
                }
            }
        }
    }

    private final class ChatEventListener extends AbstractJEventListener<ChatEvent> {

        private ChatEventListener() {
            super(ChatEvent.class);
        }

        @Override
        public void onEvent(@Nonnull ChatEvent event) {
            synchronized (userChatsCache) {

                final Chat eventChat = event.getChat();
                if (event.getType() == ChatEventType.changed) {
                    for (List<Chat> chats : userChatsCache.values()) {
                        for (int i = 0; i < chats.size(); i++) {
                            final Chat chat = chats.get(i);
                            if (chat.equals(eventChat)) {
                                chats.set(i, eventChat);
                            }
                        }
                    }
                }

            }
        }
    }

}
