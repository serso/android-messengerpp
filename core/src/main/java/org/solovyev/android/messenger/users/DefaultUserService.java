package org.solovyev.android.messenger.users;

import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.http.OnImageLoadedListener;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private ImageLoader imageLoader;

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

    @Nonnull
    private final Object lock = new Object();

    @Nonnull
    private final JEventListeners<JEventListener<? extends UserEvent>, UserEvent> listeners = Listeners.newEventListenersBuilderFor(UserEvent.class).withHardReferences().onCallerThread().create();

    // key: realm user, value: list of user contacts
    @Nonnull
    private final Map<RealmEntity, List<User>> userContactsCache = new HashMap<RealmEntity, List<User>>();

    // key: realm user, value: list of user chats
    @Nonnull
    private final Map<RealmEntity, List<Chat>> userChatsCache = new HashMap<RealmEntity, List<Chat>>();

    // key: realm user, value: user object
    @Nonnull
    private final Map<RealmEntity, User> usersCache = new HashMap<RealmEntity, User>();

    public DefaultUserService() {
        listeners.addListener(new UserEventListener());
    }

    @Override
    public void init() {
        chatService.addListener(new ChatEventListener());
    }

    @Nonnull
    @Override
    public User getUserById(@Nonnull RealmEntity realmUser) {
        return getUserById(realmUser, true);
    }

    @Nonnull
    @Override
    public User getUserById(@Nonnull RealmEntity realmUser, boolean tryFindInRealm) {
        boolean saved = true;

        User result;

        synchronized (usersCache) {
            result = usersCache.get(realmUser);
        }

        if (result == null) {
            result = getUserDao().loadUserById(realmUser.getEntityId());
            if (result == null) {
                saved = false;
            }

            if (result == null) {
                if (tryFindInRealm) {
                    final Realm realm = getRealmByUser(realmUser);
                    result = realm.getRealmUserService().getUserById(realmUser.getRealmEntityId());
                }
            }

            if (result == null) {
                result = Users.newEmptyUser(realmUser);
            } else {
                // user was loaded either from dao or from API => cache
                synchronized (usersCache) {
                    usersCache.put(realmUser, result);
                }
            }

            if (!saved) {
                insertUser(result);
            }
        }

        return result;
    }

    @Nonnull
    private Realm getRealmByUser(@Nonnull RealmEntity realmUser) {
        return realmService.getRealmById(realmUser.getRealmId());
    }

    private void insertUser(@Nonnull User user) {
        boolean inserted = false;

        synchronized (lock) {
            final User userFromDb = getUserDao().loadUserById(user.getRealmEntity().getEntityId());
            if (userFromDb == null) {
                inserted = true;
                getUserDao().insertUser(user);
            }
        }

        if (inserted) {
            listeners.fireEvent(UserEventType.added.newEvent(user));
        }
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull RealmEntity realmUser) {
        List<User> result;

        synchronized (userContactsCache) {
            result = userContactsCache.get(realmUser);
            if (result == null) {
                result = getUserDao().loadUserContacts(realmUser.getEntityId());
                if (!Collections.isEmpty(result)) {
                    userContactsCache.put(realmUser, result);
                }
            }

            // result list might be in cache and might updates due to some user events => must COPY
            return new ArrayList<User>(result);
        }
    }

    @Nonnull
    @Override
    public List<Chat> getUserChats(@Nonnull RealmEntity realmUser) {
        List<Chat> result;

        synchronized (userChatsCache) {
            result = userChatsCache.get(realmUser);
            if (result == null) {
                result = getChatService().loadUserChats(realmUser);
                if (!Collections.isEmpty(result)) {
                    userChatsCache.put(realmUser, result);
                }
            }
        }

        // result list might be in cache and might updates due to some user events => must COPY
        return new ArrayList<Chat>(result);
    }

    @Nonnull
    @Override
    public Chat getPrivateChat(@Nonnull RealmEntity realmUser1, @Nonnull final RealmEntity realmUser2) {
        final RealmEntity realmChat = getChatService().newPrivateChatId(realmUser1, realmUser2);

        Chat result = getChatService().getChatById(realmChat);
        if (result == null) {
            result = getChatService().newPrivateChat(realmUser1, realmUser2);
        }

        return result;
    }

    @Nonnull
    @Override
    public List<User> getOnlineUserContacts(@Nonnull RealmEntity realmUser) {
        return Lists.newArrayList(Iterables.filter(getUserContacts(realmUser), new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User contact) {
                return contact != null && contact.isOnline();
            }
        }));
    }

    @Override
    public void updateUser(@Nonnull User user) {
        synchronized (lock) {
            getUserDao().updateUser(user);
        }

        listeners.fireEvent(UserEventType.changed.newEvent(user));
    }

    @Override
    public void removeUsersInRealm(@Nonnull final String realmId) {
        synchronized (lock) {
            Iterators.removeIf(this.userChatsCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));
            Iterators.removeIf(this.userContactsCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));
            Iterators.removeIf(this.usersCache.entrySet().iterator(), RealmMapEntryMatcher.forRealm(realmId));

            this.userDao.deleteAllUsersInRealm(realmId);
        }
    }

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @Override
    public void syncUserProperties(@Nonnull RealmEntity realmUser) {
        final User user = getRealmByUser(realmUser).getRealmUserService().getUserById(realmUser.getRealmEntityId());
        if (user != null) {
            synchronized (lock) {
                getUserDao().updateUser(user);
            }
            listeners.fireEvent(UserEventType.changed.newEvent(user));
        }
    }

    @Override
    @Nonnull
    public List<User> syncUserContacts(@Nonnull RealmEntity realmUser) {
        final List<User> contacts = getRealmByUser(realmUser).getRealmUserService().getUserContacts(realmUser.getRealmEntityId());
        synchronized (userContactsCache) {
            userContactsCache.put(realmUser, contacts);
        }

        mergeUserContacts(realmUser, contacts, false, true);

        return java.util.Collections.unmodifiableList(contacts);
    }

    @Override
    public void mergeUserContacts(@Nonnull RealmEntity realmUser, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
        User user = getUserById(realmUser);
        final MergeDaoResult<User, String> result;
        synchronized (lock) {
            result = getUserDao().mergeUserContacts(realmUser.getEntityId(), contacts, allowRemoval, allowUpdate);

            // update sync data
            user = user.updateContactsSyncDate();
            updateUser(user);
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
    public List<Chat> syncUserChats(@Nonnull RealmEntity realmUser) {
        final List<ApiChat> apiChats = getRealmByUser(realmUser).getRealmChatService().getUserChats(realmUser.getRealmEntityId());

        final List<Chat> chats = Lists.newArrayList(Iterables.transform(apiChats, new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat input) {
                assert input != null;
                return input.getChat();
            }
        }));

        synchronized (userChatsCache) {
            userChatsCache.put(realmUser, chats);
        }

        mergeUserChats(realmUser, apiChats);

        return java.util.Collections.unmodifiableList(chats);
    }

    @Override
    public void mergeUserChats(@Nonnull RealmEntity realmUser, @Nonnull List<? extends ApiChat> apiChats) {
        User user = this.getUserById(realmUser);

        final MergeDaoResult<ApiChat, String> result;
        synchronized (lock) {
            result = getChatService().mergeUserChats(realmUser.getEntityId(), apiChats);

            // update sync data
            user = user.updateChatsSyncDate();
            updateUser(user);
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
        getChatService().fireEvents(chatEvents);
    }

    @Nonnull
    private ChatService getChatService() {
        return this.chatService;
    }

    @Override
    public void checkOnlineUserContacts(@Nonnull RealmEntity realmUser) {
        final List<User> contacts = getRealmByUser(realmUser).getRealmUserService().checkOnlineUsers(getUserContacts(realmUser));

        final User user = getUserById(realmUser);

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        for (User contact : contacts) {
            final UserEventType type = contact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline;
            userEvents.add(type.newEvent(user, contact));
        }

        listeners.fireEvents(userEvents);

    }

    @Override
    public void fetchUserIcons(@Nonnull User user) {
        this.fetchUserIcon(user);
        this.fetchContactsIcons(user);

        // update sync data
        user = user.updateContactsSyncDate();
        updateUser(user);
    }

    @Override
    public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
        final RealmDef realmDef = getRealmByUser(user.getRealmEntity()).getRealmDef();

        Drawable defaultUserIcon = realmDef.getDefaultUserIcon();
        if ( defaultUserIcon == null ){
            defaultUserIcon = getDefaultUserIcon();
        }

        final BitmapDrawable userIcon = realmDef.getUserIcon(user);
        if (userIcon == null) {
            final String userIconUri = realmDef.getUserIconUri(user);
            if (!Strings.isEmpty(userIconUri)) {
                this.imageLoader.loadImage(userIconUri, imageView, getDefaultUserIconResId());
            } else {
                imageView.setImageDrawable(defaultUserIcon);
            }
        } else {
            imageView.setImageDrawable(userIcon);
        }
    }

    @Override
    @Nonnull
    public Drawable getDefaultUserIcon() {
        return context.getResources().getDrawable(getDefaultUserIconResId());
    }

    private int getDefaultUserIconResId() {
        return R.drawable.mpp_empty_user_icon;
    }

    @Override
    public void setUserIcon(@Nonnull User user, @Nonnull OnImageLoadedListener imageLoadedListener) {
        final RealmDef realmDef = getRealmByUser(user.getRealmEntity()).getRealmDef();

        final BitmapDrawable userIcon = realmDef.getUserIcon(user);
        if (userIcon == null) {
            final String userIconUri = realmDef.getUserIconUri(user);
            if (!Strings.isEmpty(userIconUri)) {
                this.imageLoader.loadImage(userIconUri, imageLoadedListener);
            } else {
                imageLoadedListener.setDefaultImage();
            }
        } else {
            imageLoadedListener.onImageLoaded(userIcon.getBitmap());
        }
    }

    @Override
    public void setUserPhoto(@Nonnull ImageView imageView, @Nonnull User user) {
        final RealmDef realmDef = getRealmByUser(user.getRealmEntity()).getRealmDef();

        Drawable defaultUserIcon = realmDef.getDefaultUserIcon();
        if (defaultUserIcon == null) {
            defaultUserIcon = getDefaultUserIcon();
        }
        final BitmapDrawable userIcon = realmDef.getUserIcon(user);
        if (userIcon == null) {
            final String userPhotoUri = getUserPhotoUri(user);
            if (!Strings.isEmpty(userPhotoUri)) {
                this.imageLoader.loadImage(userPhotoUri, imageView, getDefaultUserIconResId());
            } else {
                imageView.setImageDrawable(defaultUserIcon);
            }
        } else {
            imageView.setImageDrawable(userIcon);
        }
    }

    public void fetchUserIcon(@Nonnull User user) {
        final RealmDef realmDef = getRealmByUser(user.getRealmEntity()).getRealmDef();
        final String userIconUri = realmDef.getUserIconUri(user);
        if (!Strings.isEmpty(userIconUri)) {
            assert userIconUri != null;
            this.imageLoader.loadImage(userIconUri);
        }
    }

    public void fetchContactsIcons(@Nonnull User user) {
        for (User contact : getUserContacts(user.getRealmEntity())) {
            fetchUserIcon(contact);
        }
    }

    @Nullable
    private String getUserPhotoUri(@Nonnull User user) {
        String result = user.getPropertyValueByName("photoRec");
        if ( result == null ) {
            result = user.getPropertyValueByName("photoBig");
        }
        return result;
    }

    @Nonnull
    private UserDao getUserDao() {
        return userDao;
    }

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    @Override
    public void fireEvent(@Nonnull UserEvent event) {
        this.listeners.fireEvent(event);
    }

    @Override
    public void fireEvents(@Nonnull Collection<UserEvent> events) {
        this.listeners.fireEvents(events);
    }

    @Override
    public boolean addListener(@Nonnull JEventListener<UserEvent> listener) {
        return this.listeners.addListener(listener);
    }

    @Override
    public boolean removeListener(@Nonnull JEventListener<UserEvent> listener) {
        return this.listeners.removeListener(listener);
    }

    @Override
    public void removeListeners() {
        this.listeners.removeListeners();
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
                    final List<User> contacts = userContactsCache.get(eventUser.getRealmEntity());
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
                    final List<User> contactsFromCache = userContactsCache.get(eventUser.getRealmEntity());
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
                    final List<User> contacts = userContactsCache.get(eventUser.getRealmEntity());
                    if (contacts != null) {
                        Iterables.removeIf(contacts, new Predicate<User>() {
                            @Override
                            public boolean apply(@javax.annotation.Nullable User contact) {
                                return contact != null && contact.getRealmEntity().getEntityId().equals(removedContactId);
                            }
                        });
                    }
                }
            }

            synchronized (userChatsCache) {
                if (type == UserEventType.chat_added) {
                    final Chat chat = event.getDataAsChat();
                    final List<Chat> chats = userChatsCache.get(eventUser.getRealmEntity());
                    if (chats != null) {
                        if (!Iterables.contains(chats, chat)) {
                            chats.add(chat);
                        }
                    }
                }

                if (type == UserEventType.chat_added_batch) {
                    final List<Chat> chats = event.getDataAsChats();
                    final List<Chat> chatsFromCache = userChatsCache.get(eventUser.getRealmEntity());
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
                    final List<Chat> chats = userChatsCache.get(eventUser.getRealmEntity());
                    if (chats != null) {
                        chats.remove(chat);
                    }
                }
            }

            synchronized (usersCache) {
                if (type == UserEventType.changed) {
                    usersCache.put(eventUser.getRealmEntity(), eventUser);
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
