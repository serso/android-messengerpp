package org.solovyev.android.messenger.users;

import android.content.Context;
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
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.realms.Realm;
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
    private Realm realm;

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private Provider<UserDao> userDaoProvider;

    @Inject
    @NotNull
    private ImageLoader imageLoader;

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

    // key: user id, value: list of user contacts
    @NotNull
    private final Map<String, List<User>> userContactsCache = new HashMap<String, List<User>>();

    // key: user id, value: list of user chats
    @NotNull
    private final Map<String, List<Chat>> userChatsCache = new HashMap<String, List<Chat>>();

    // key: user id, value: user object
    @NotNull
    private final Map<String, User> usersCache = new HashMap<String, User>();

    public DefaultUserService() {
        listeners.addListener(this);
    }

    @Override
    public void init() {
        chatService.addChatEventListener(this);
    }

    @NotNull
    @Override
    public User getUserById(@NotNull String userId, @NotNull Context context) {
        boolean saved = true;

        User result;

        synchronized (usersCache) {
            result = usersCache.get(userId);
        }

        if (result == null) {
            result = getUserDao(context).loadUserById(userId);
            if (result == null) {
                saved = false;
            }

            if (result == null) {
                result = realm.getRealmUserService().getUserById(userId);
            }

            if (result == null) {
                result = UserImpl.newFakeInstance(userId);
            } else {
                // user was loaded either from dao or from API => cache
                synchronized (usersCache) {
                    usersCache.put(userId, result);
                }
            }

            if (!saved) {
                insertUser(context, result);
            }
        }

        return result;
    }

    private void insertUser(@NotNull Context context, @NotNull User user) {
        boolean inserted = false;

        synchronized (lock) {
            final User userFromDb = getUserDao(context).loadUserById(user.getId());
            if (userFromDb == null) {
                inserted = true;
                getUserDao(context).insertUser(user);
            }
        }

        if (inserted) {
            listeners.fireUserEvent(user, UserEventType.added, null);
        }
    }

    @NotNull
    @Override
    public List<User> getUserContacts(@NotNull String userId, @NotNull Context context) {
        List<User> result;

        synchronized (userContactsCache) {
            result = userContactsCache.get(userId);
            if (result == null) {
                result = getUserDao(context).loadUserContacts(userId);
                if (!Collections.isEmpty(result)) {
                    userContactsCache.put(userId, result);
                }
            }

            // result list might be in cache and might updates due to some user events => must COPY
            return new ArrayList<User>(result);
        }
    }

    @NotNull
    @Override
    public List<Chat> getUserChats(@NotNull String userId, @NotNull Context context) {
        List<Chat> result;

        synchronized (userChatsCache) {
            result = userChatsCache.get(userId);
            if (result == null) {
                result = getChatService().loadUserChats(userId, context);
                if (!Collections.isEmpty(result)) {
                    userChatsCache.put(userId, result);
                }
            }
        }

        // result list might be in cache and might updates due to some user events => must COPY
        return new ArrayList<Chat>(result);
    }

    @NotNull
    @Override
    public Chat getPrivateChat(@NotNull String userId, @NotNull final String secondUserId, @NotNull final Context context) {
        final String chatId = getChatService().createPrivateChatId(userId, secondUserId);

        Chat result = getChatService().getChatById(chatId, context);
        if (result == null) {
            result = getChatService().createPrivateChat(userId, secondUserId, context);
        }

        return result;
    }

    @NotNull
    @Override
    public List<User> getOnlineUserContacts(@NotNull String userId, @NotNull Context context) {
        return Lists.newArrayList(Iterables.filter(getUserContacts(userId, context), new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User contact) {
                return contact != null && contact.isOnline();
            }
        }));
    }

    @Override
    public void updateUser(@NotNull User user, @NotNull Context context) {
        synchronized (lock) {
            getUserDao(context).updateUser(user);
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
    public void syncUserProperties(@NotNull String userId, @NotNull Context context) {
        final User user = realm.getRealmUserService().getUserById(userId);
        if (user != null) {
            synchronized (lock) {
                getUserDao(context).updateUser(user);
            }
            listeners.fireUserEvent(user, UserEventType.changed, null);
        }
    }

    @Override
    @NotNull
    public List<User> syncUserContacts(@NotNull String userId, @NotNull Context context) {
        final List<User> contacts = realm.getRealmUserService().getUserContacts(userId);
        synchronized (userContactsCache) {
            userContactsCache.put(userId, contacts);
        }

        User user = getUserById(userId, context);
        final MergeDaoResult<User, String> result;
        synchronized (lock) {
            result = getUserDao(context).mergeUserContacts(userId, contacts);

            // update sync data
            user = user.updateContactsSyncDate();
            updateUser(user, context);
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
    public List<Chat> syncUserChats(@NotNull String userId, @NotNull Context context) {
        final List<ApiChat> apiChats = realm.getRealmChatService().getUserChats(userId, context);

        final List<Chat> chats = Lists.newArrayList(Iterables.transform(apiChats, new Function<ApiChat, Chat>() {
            @Override
            public Chat apply(@javax.annotation.Nullable ApiChat input) {
                assert input != null;
                return input.getChat();
            }
        }));

        synchronized (userChatsCache) {
            userChatsCache.put(userId, chats);
        }

        mergeUserChats(userId, apiChats, context);

        return java.util.Collections.unmodifiableList(chats);
    }

    @Override
    public void mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> apiChats, @NotNull Context context) {
        User user = this.getUserById(userId, context);

        final MergeDaoResult<ApiChat, String> result;
        synchronized (lock) {
            result = getChatService().mergeUserChats(userId, apiChats, context);

            // update sync data
            user = user.updateChatsSyncDate();
            updateUser(user, context);
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
    public void checkOnlineUserContacts(@NotNull String userId, @NotNull Context context) {
        final List<User> contacts = realm.getRealmUserService().checkOnlineUsers(getUserContacts(userId, context));

        final User user = getUserById(userId, context);

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

        for (User contact : contacts) {
            userEvents.add(new UserEvent(user, contact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline, contact));
        }

        listeners.fireUserEvents(userEvents);

    }

    @Override
    public void fetchUserIcons(@NotNull User user, @NotNull Context context) {
        this.fetchUserIcon(user, context);
        this.fetchContactsIcons(user, context);

        // update sync data
        user = user.updateContactsSyncDate();
        updateUser(user, context);
    }

    @Override
    public void setUserIcon(@NotNull User user, @NotNull Context context, @NotNull ImageView imageView) {
        final Drawable defaultUserIcon = getDefaultUserIcon(context);

        final String userIconUri = getUserIconUri(user, context);
        if (!Strings.isEmpty(userIconUri)) {
            this.imageLoader.loadImage(userIconUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultUserIcon);
        }
    }

    @Override
    @NotNull
    public Drawable getDefaultUserIcon(@NotNull Context context) {
        return context.getResources().getDrawable(R.drawable.empty_icon);
    }

    @Override
    public void setUserIcon(@NotNull User user, @NotNull Context context, @NotNull OnImageLoadedListener imageLoadedListener) {
        final String userIconUri = getUserIconUri(user, context);
        if (!Strings.isEmpty(userIconUri)) {
            this.imageLoader.loadImage(userIconUri, imageLoadedListener);
        } else {
            imageLoadedListener.setDefaultImage();
        }
    }

    @Override
    public void setUserPhoto(@NotNull ImageView imageView, @NotNull User user, @NotNull Context context) {
        final Drawable defaultUserIcon = getDefaultUserIcon(context);

        final String userPhotoUri = getUserPhotoUri(user, context);
        if (!Strings.isEmpty(userPhotoUri)) {
            this.imageLoader.loadImage(userPhotoUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultUserIcon);
        }
    }

    public void fetchUserIcon(@NotNull User user, @NotNull Context context) {
        final String userIconUri = getUserIconUri(user, context);
        if (!Strings.isEmpty(userIconUri)) {
            assert userIconUri != null;
            this.imageLoader.loadImage(userIconUri);
        }
    }

    public void fetchContactsIcons(@NotNull User user, @NotNull Context context) {
        for (User contact : getUserContacts(user.getId(), context)) {
            fetchUserIcon(contact, context);
        }
    }

    @Nullable
    private String getUserIconUri(@NotNull User user, @NotNull Context context) {
        return user.getPropertyValueByName("photo");
    }

    @Nullable
    private String getUserPhotoUri(@NotNull User user, @NotNull Context context) {
        String result = user.getPropertyValueByName("photoRec");
        if ( result == null ) {
            result = user.getPropertyValueByName("photoBig");
        }
        return result;
    }

    @NotNull
    private UserDao getUserDao(@NotNull Context context) {
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
                final List<User> contacts = userContactsCache.get(eventUser.getId());
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
                final List<User> contactsFromCache = userContactsCache.get(eventUser.getId());
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
                final List<User> contacts = userContactsCache.get(eventUser.getId());
                if (contacts != null) {
                    Iterables.removeIf(contacts, new Predicate<User>() {
                        @Override
                        public boolean apply(@javax.annotation.Nullable User contact) {
                            return contact != null && contact.getId().equals(removedContactId);
                        }
                    });
                }
            }
        }

        synchronized (userChatsCache) {
            if (userEventType == UserEventType.chat_added) {
                if (data instanceof Chat) {
                    final Chat chat = ((Chat) data);
                    final List<Chat> chats = userChatsCache.get(eventUser.getId());
                    if (chats != null) {
                        if (!Iterables.contains(chats, chat)) {
                            chats.add(chat);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.chat_added_batch) {
                final List<Chat> chats = (List<Chat>) data;
                final List<Chat> chatsFromCache = userChatsCache.get(eventUser.getId());
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
                final List<Chat> chats = userChatsCache.get(eventUser.getId());
                if (chats != null) {
                    chats.remove(chat);
                }
            }
        }

        synchronized (usersCache) {
            if (userEventType == UserEventType.changed) {
                usersCache.put(eventUser.getId(), eventUser);
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
