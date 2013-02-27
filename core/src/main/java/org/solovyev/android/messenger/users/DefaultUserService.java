package org.solovyev.android.messenger.users;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.http.OnImageLoadedListener;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEventContainer;
import org.solovyev.android.messenger.chats.ChatEventListener;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.roboguice.RoboGuiceUtils;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
@Singleton
public class DefaultUserService implements UserService, UserEventListener, ChatEventListener {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */
    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private Provider<UserDao> userDaoProvider;

    @Inject
    @NotNull
    private ImageLoader imageLoader;

    @Inject
    @NotNull
    private Application context;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @NotNull
    private final Object lock = new Object();

    @NotNull
    private final UserEventListeners listeners = new ListUserEventListeners();

    // key: realm user, value: list of user contacts
    @NotNull
    private final Map<RealmEntity, List<User>> userContactsCache = new HashMap<RealmEntity, List<User>>();

    // key: realm user, value: list of user chats
    @NotNull
    private final Map<RealmEntity, List<Chat>> userChatsCache = new HashMap<RealmEntity, List<Chat>>();

    // key: realm user, value: user object
    @NotNull
    private final Map<RealmEntity, User> usersCache = new HashMap<RealmEntity, User>();

    public DefaultUserService() {
        listeners.addListener(this);
    }

    @Override
    public void init() {
        chatService.addChatEventListener(this);
    }

    @NotNull
    @Override
    public User getUserById(@NotNull RealmEntity realmUser) {
        return getUserById(realmUser, true);
    }

    @NotNull
    @Override
    public User getUserById(@NotNull RealmEntity realmUser, boolean tryFindInRealm) {
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
                result = UserImpl.newFakeInstance(realmUser);
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

    @NotNull
    private Realm getRealmByUser(@NotNull RealmEntity realmUser) {
        return realmService.getRealmById(realmUser.getRealmId());
    }

    private void insertUser(@NotNull User user) {
        boolean inserted = false;

        synchronized (lock) {
            final User userFromDb = getUserDao().loadUserById(user.getRealmUser().getEntityId());
            if (userFromDb == null) {
                inserted = true;
                getUserDao().insertUser(user);
            }
        }

        if (inserted) {
            listeners.fireUserEvent(user, UserEventType.added, null);
        }
    }

    @NotNull
    @Override
    public List<User> getUserContacts(@NotNull RealmEntity realmUser) {
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

    @NotNull
    @Override
    public List<Chat> getUserChats(@NotNull RealmEntity realmUser) {
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

    @NotNull
    @Override
    public Chat getPrivateChat(@NotNull RealmEntity realmUser, @NotNull final RealmEntity secondRealmUser) {
        final RealmEntity realmChat = getChatService().createPrivateChatId(realmUser, secondRealmUser);

        Chat result = getChatService().getChatById(realmChat);
        if (result == null) {
            result = getChatService().createPrivateChat(realmUser, secondRealmUser);
        }

        return result;
    }

    @NotNull
    @Override
    public List<User> getOnlineUserContacts(@NotNull RealmEntity realmUser) {
        return Lists.newArrayList(Iterables.filter(getUserContacts(realmUser), new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User contact) {
                return contact != null && contact.isOnline();
            }
        }));
    }

    @Override
    public void updateUser(@NotNull User user) {
        synchronized (lock) {
            getUserDao().updateUser(user);
        }

        listeners.fireUserEvent(user, UserEventType.changed, null);
    }

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    @Override
    public void syncUserProperties(@NotNull RealmEntity realmUser) {
        final User user = getRealmByUser(realmUser).getRealmUserService().getUserById(realmUser.getRealmEntityId());
        if (user != null) {
            synchronized (lock) {
                getUserDao().updateUser(user);
            }
            listeners.fireUserEvent(user, UserEventType.changed, null);
        }
    }

    @Override
    @NotNull
    public List<User> syncUserContacts(@NotNull RealmEntity realmUser) {
        final List<User> contacts = getRealmByUser(realmUser).getRealmUserService().getUserContacts(realmUser.getRealmEntityId());
        synchronized (userContactsCache) {
            userContactsCache.put(realmUser, contacts);
        }

        User user = getUserById(realmUser);
        final MergeDaoResult<User, String> result;
        synchronized (lock) {
            result = getUserDao().mergeUserContacts(realmUser.getEntityId(), contacts);

            // update sync data
            user = user.updateContactsSyncDate();
            updateUser(user);
        }

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        userEvents.add(new UserEvent(user, UserEventType.contact_added_batch, result.getAddedObjectLinks()));

        final List<User> addedContacts = result.getAddedObjects();
        for (User addedContact : addedContacts) {
            userEvents.add(new UserEvent(addedContact, UserEventType.added, null));
        }
        userEvents.add(new UserEvent(user, UserEventType.contact_added_batch, addedContacts));


        for (String removedContactId : result.getRemovedObjectIds()) {
            userEvents.add(new UserEvent(user, UserEventType.contact_removed, removedContactId));
        }

        for (User updatedContact : result.getUpdatedObjects()) {
            userEvents.add(new UserEvent(updatedContact, UserEventType.changed, null));
            userEvents.add(new UserEvent(user, updatedContact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline, updatedContact));
        }

        listeners.fireUserEvents(userEvents);

        return java.util.Collections.unmodifiableList(contacts);
    }

    @NotNull
    @Override
    public List<Chat> syncUserChats(@NotNull RealmEntity realmUser) {
        final List<ApiChat> apiChats = getRealmByUser(realmUser).getRealmChatService().getUserChats(realmUser.getRealmEntityId(), context);

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
    public void mergeUserChats(@NotNull RealmEntity realmUser, @NotNull List<? extends ApiChat> apiChats) {
        User user = this.getUserById(realmUser);

        final MergeDaoResult<ApiChat, String> result;
        synchronized (lock) {
            result = getChatService().mergeUserChats(realmUser.getEntityId(), apiChats);

            // update sync data
            user = user.updateChatsSyncDate();
            updateUser(user);
        }

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(apiChats.size());
        final List<ChatEventContainer.ChatEvent> chatEvents = new ArrayList<ChatEventContainer.ChatEvent>(apiChats.size());

        final List<Chat> addedChatLinks = Lists.transform(result.getAddedObjectLinks(), new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
                assert apiChat != null;
                return apiChat.getChat();
            }
        });
        if (!addedChatLinks.isEmpty()) {
            userEvents.add(new UserEvent(user, UserEventType.chat_added_batch, addedChatLinks));
        }

        final List<Chat> addedChats = Lists.transform(result.getAddedObjects(), new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
                assert apiChat != null;
                return apiChat.getChat();
            }
        });

        for (Chat addedChat : addedChats) {
            chatEvents.add(new ChatEventContainer.ChatEvent(addedChat, ChatEventType.added, null));
        }
        if (!addedChats.isEmpty()) {
            userEvents.add(new UserEvent(user, UserEventType.chat_added_batch, addedChats));
        }

        for (String removedChatId : result.getRemovedObjectIds()) {
            userEvents.add(new UserEvent(user, UserEventType.chat_removed, removedChatId));
        }

        for (ApiChat updatedChat : result.getUpdatedObjects()) {
            chatEvents.add(new ChatEventContainer.ChatEvent(updatedChat.getChat(), ChatEventType.changed, null));
        }

        listeners.fireUserEvents(userEvents);
        getChatService().fireChatEvents(chatEvents);
    }

    @NotNull
    private ChatService getChatService() {
        return this.chatService;
    }

    @Override
    public void checkOnlineUserContacts(@NotNull RealmEntity realmUser) {
        final List<User> contacts = getRealmByUser(realmUser).getRealmUserService().checkOnlineUsers(getUserContacts(realmUser));

        final User user = getUserById(realmUser);

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        for (User contact : contacts) {
            userEvents.add(new UserEvent(user, contact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline, contact));
        }

        listeners.fireUserEvents(userEvents);

    }

    @Override
    public void fetchUserIcons(@NotNull User user) {
        this.fetchUserIcon(user);
        this.fetchContactsIcons(user);

        // update sync data
        user = user.updateContactsSyncDate();
        updateUser(user);
    }

    @Override
    public void setUserIcon(@NotNull User user, @NotNull ImageView imageView) {
        final Drawable defaultUserIcon = getDefaultUserIcon();

        final String userIconUri = getUserIconUri(user);
        if (!Strings.isEmpty(userIconUri)) {
            this.imageLoader.loadImage(userIconUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultUserIcon);
        }
    }

    @Override
    @NotNull
    public Drawable getDefaultUserIcon() {
        return context.getResources().getDrawable(R.drawable.empty_icon);
    }

    @Override
    public void setUserIcon(@NotNull User user, @NotNull OnImageLoadedListener imageLoadedListener) {
        final String userIconUri = getUserIconUri(user);
        if (!Strings.isEmpty(userIconUri)) {
            this.imageLoader.loadImage(userIconUri, imageLoadedListener);
        } else {
            imageLoadedListener.setDefaultImage();
        }
    }

    @Override
    public void setUserPhoto(@NotNull ImageView imageView, @NotNull User user) {
        final Drawable defaultUserIcon = getDefaultUserIcon();

        final String userPhotoUri = getUserPhotoUri(user);
        if (!Strings.isEmpty(userPhotoUri)) {
            this.imageLoader.loadImage(userPhotoUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultUserIcon);
        }
    }

    public void fetchUserIcon(@NotNull User user) {
        final String userIconUri = getUserIconUri(user);
        if (!Strings.isEmpty(userIconUri)) {
            assert userIconUri != null;
            this.imageLoader.loadImage(userIconUri);
        }
    }

    public void fetchContactsIcons(@NotNull User user) {
        for (User contact : getUserContacts(user.getRealmUser())) {
            fetchUserIcon(contact);
        }
    }

    @Nullable
    private String getUserIconUri(@NotNull User user) {
        return user.getPropertyValueByName("photo");
    }

    @Nullable
    private String getUserPhotoUri(@NotNull User user) {
        String result = user.getPropertyValueByName("photoRec");
        if ( result == null ) {
            result = user.getPropertyValueByName("photoBig");
        }
        return result;
    }

    @NotNull
    private UserDao getUserDao() {
        return RoboGuiceUtils.getInContextScope(context, userDaoProvider);
    }

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    @Override
    public boolean addListener(@NotNull UserEventListener userEventListener) {
        return listeners.addListener(userEventListener);
    }

    @Override
    public boolean removeListener(@NotNull UserEventListener userEventListener) {
        return listeners.removeListener(userEventListener);
    }

    @Override
    public void fireUserEvent(@NotNull User user, @NotNull UserEventType userEventType, @Nullable Object data) {
        listeners.fireUserEvent(user, userEventType, data);
    }

    @Override
    public void fireUserEvents(@NotNull List<UserEvent> userEvents) {
        listeners.fireUserEvents(userEvents);
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {

        synchronized (userContactsCache) {

            if (userEventType == UserEventType.changed) {
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

            if (userEventType == UserEventType.contact_added) {
                // contact added => need to add to list of cached contacts
                final User contact = ((User) data);
                final List<User> contacts = userContactsCache.get(eventUser.getRealmUser());
                if (contacts != null) {
                    // check if not contains as can be added in parallel
                    if (!Iterables.contains(contacts, contact)) {
                        contacts.add(contact);
                    }
                }
            }

            if (userEventType == UserEventType.contact_added_batch) {
                // contacts added => need to add to list of cached contacts
                final List<User> contacts = (List<User>) data;
                final List<User> contactsFromCache = userContactsCache.get(eventUser.getRealmUser());
                if (contactsFromCache != null) {
                    for (User contact : contacts) {
                        // check if not contains as can be added in parallel
                        if (!Iterables.contains(contactsFromCache, contact)) {
                            contactsFromCache.add(contact);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.contact_removed) {
                // contact removed => try to remove from cached contacts
                final String removedContactId = ((String) data);
                final List<User> contacts = userContactsCache.get(eventUser.getRealmUser());
                if (contacts != null) {
                    Iterables.removeIf(contacts, new Predicate<User>() {
                        @Override
                        public boolean apply(@javax.annotation.Nullable User contact) {
                            return contact != null && contact.getRealmUser().getEntityId().equals(removedContactId);
                        }
                    });
                }
            }
        }

        synchronized (userChatsCache) {
            if (userEventType == UserEventType.chat_added) {
                if (data instanceof Chat) {
                    final Chat chat = ((Chat) data);
                    final List<Chat> chats = userChatsCache.get(eventUser.getRealmUser());
                    if (chats != null) {
                        if (!Iterables.contains(chats, chat)) {
                            chats.add(chat);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.chat_added_batch) {
                final List<Chat> chats = (List<Chat>) data;
                final List<Chat> chatsFromCache = userChatsCache.get(eventUser.getRealmUser());
                if (chatsFromCache != null) {
                    for (Chat chat : chats) {
                        if (!Iterables.contains(chatsFromCache, chat)) {
                            chatsFromCache.add(chat);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.chat_removed) {
                final Chat chat = ((Chat) data);
                final List<Chat> chats = userChatsCache.get(eventUser.getRealmUser());
                if (chats != null) {
                    chats.remove(chat);
                }
            }
        }

        synchronized (usersCache) {
            if (userEventType == UserEventType.changed) {
                usersCache.put(eventUser.getRealmUser(), eventUser);
            }
        }
    }

    @Override
    public void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data) {
        synchronized (userChatsCache) {

            if (chatEventType == ChatEventType.changed) {
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
