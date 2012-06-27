package org.solovyev.android.messenger.users;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.VersionedEntityImpl;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.common.utils.CollectionsUtils;
import org.solovyev.common.utils.StringUtils;

import java.util.*;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
public class DefaultUserService implements UserService, UserEventListener, ChatEventListener {

    @NotNull
    private final Object lock = new Object();

    @NotNull
    private final UserEventContainer listeners = new ListUserEventContainer();

    // key: user id, value: list of user friends
    @NotNull
    private final Map<Integer, List<User>> userFriendsCache = new HashMap<Integer, List<User>>();

    // key: user id, value: list of user chats
    @NotNull
    private final Map<Integer, List<Chat>> userChatsCache = new HashMap<Integer, List<Chat>>();

    // key: user id, value: user object
    @NotNull
    private final Map<Integer, User> usersCache = new HashMap<Integer, User>();


    public DefaultUserService() {
        listeners.addUserEventListener(this);
    }

    @NotNull
    @Override
    public User getUserById(@NotNull Integer userId, @NotNull Context context) {
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
                result = getApiUserService().getUserById(userId);
            }

            if (result == null) {
                result = UserImpl.newInstance(new VersionedEntityImpl(userId));
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
    public List<User> getUserFriends(@NotNull Integer userId, @NotNull Context context) {
        List<User> result;

        synchronized (userFriendsCache) {
            result = userFriendsCache.get(userId);
            if (result == null) {
                result = getUserDao(context).loadUserFriends(userId);
                if (!CollectionsUtils.isEmpty(result)) {
                    userFriendsCache.put(userId, result);
                }
            }
        }

        // result list might be in cache and might updates due to some user events => must COPY
        return new ArrayList<User>(result);
    }

    @NotNull
    @Override
    public List<Chat> getUserChats(@NotNull Integer userId, @NotNull Context context) {
        List<Chat> result;

        synchronized (userChatsCache) {
            result = userChatsCache.get(userId);
            if (result == null) {
                result = getChatService().loadUserChats(userId, context);
                if (!CollectionsUtils.isEmpty(result)) {
                    userChatsCache.put(userId, result);
                }
            }
        }

        // result list might be in cache and might updates due to some user events => must COPY
        return new ArrayList<Chat>(result);
    }

    @NotNull
    @Override
    public Chat getPrivateChat(@NotNull Integer userId, @NotNull final Integer secondUserId, @NotNull final Context context) {
        Chat result;

        try {
            result = Iterables.find(getUserChats(userId, context), new Predicate<Chat>() {
                @Override
                public boolean apply(@javax.annotation.Nullable Chat chat) {
                    assert chat != null;

                    if (chat.isPrivate()) {

                        final List<User> participants = getChatService().getParticipants(chat.getId(), context);
                        return Iterables.any(participants, new Predicate<User>() {
                            @Override
                            public boolean apply(@javax.annotation.Nullable User participant) {
                                assert participant != null;
                                return secondUserId.equals(participant.getId());
                            }
                        });

                    } else {
                        return false;
                    }
                }
            });
        } catch (NoSuchElementException e) {
            result = getChatService().createPrivateChat(userId, secondUserId, context);
        }

        return result;
    }

    @NotNull
    @Override
    public List<User> getOnlineUserFriends(@NotNull Integer userId, @NotNull Context context) {
        return Lists.newArrayList(Iterables.filter(getUserFriends(userId, context), new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User friend) {
                return friend != null && friend.isOnline();
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
    public void syncUserProperties(@NotNull Integer userId, @NotNull Context context) {
        final User user = getApiUserService().getUserById(userId);
        if (user != null) {
            synchronized (lock) {
                getUserDao(context).updateUser(user);
            }
            listeners.fireUserEvent(user, UserEventType.changed, null);
        }
    }

    @Override
    @NotNull
    public List<User> syncUserFriends(@NotNull Integer userId, @NotNull Context context) {
        final List<User> friends = getApiUserService().getUserFriends(userId);
        synchronized (userFriendsCache) {
            userFriendsCache.put(userId, friends);
        }

        User user = getUserById(userId, context);
        final MergeDaoResult<User, Integer> result;
        synchronized (lock) {
            result = getUserDao(context).mergeUserFriends(userId, friends);

            // update sync data
            user = user.updateFriendsSyncDate();
            updateUser(user, context);
        }

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(friends.size());

        userEvents.add(new UserEvent(user, UserEventType.friend_added_batch, result.getAddedObjectLinks()));

        final List<User> addedFriends = result.getAddedObjects();
        for (User addedFriend : addedFriends) {
            userEvents.add(new UserEvent(addedFriend, UserEventType.added, null));
        }
        userEvents.add(new UserEvent(user, UserEventType.friend_added_batch, addedFriends));


        for (Integer removedFriendId : result.getRemovedObjectIds()) {
            userEvents.add(new UserEvent(user, UserEventType.friend_removed, removedFriendId));
        }

        for (User updatedFriend : result.getUpdatedObjects()) {
            userEvents.add(new UserEvent(updatedFriend, UserEventType.changed, null));
            userEvents.add(new UserEvent(user, updatedFriend.isOnline() ? UserEventType.friend_online : UserEventType.friend_offline, updatedFriend));
        }

        listeners.fireUserEvents(userEvents);

        return Collections.unmodifiableList(friends);
    }

    @NotNull
    @Override
    public List<Chat> syncUserChats(@NotNull Integer userId, @NotNull Context context) {
        final List<ApiChat> apiChats = getApiChatService().getUserChats(userId, context);

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

        return Collections.unmodifiableList(chats);
    }

    @Override
    public void mergeUserChats(@NotNull Integer userId, @NotNull List<? extends ApiChat> apiChats, @NotNull Context context) {
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
    private ApiChatService getApiChatService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getApiChatService();
    }

    @NotNull
    private ChatService getChatService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
    }

    @Override
    public void checkOnlineUserFriends(@NotNull Integer userId, @NotNull Context context) {
        final List<User> friends = getApiUserService().checkOnlineUsers(getUserFriends(userId, context));

        final User user = getUserById(userId, context);

        final List<UserEvent> userEvents = new ArrayList<UserEvent>(friends.size());

        for (User friend : friends) {
            userEvents.add(new UserEvent(user, friend.isOnline() ? UserEventType.friend_online : UserEventType.friend_offline, friend));
        }

        listeners.fireUserEvents(userEvents);

    }

    @Override
    public void fetchUserIcons(@NotNull User user, @NotNull Context context) {
        this.fetchUserIcon(user, context);
        this.fetchFriendsIcons(user, context);

        // update sync data
        user = user.updateFriendsSyncDate();
        updateUser(user, context);
    }

    @Override
    public void setUserIcon(@NotNull ImageView imageView, @NotNull User user, @NotNull Context context) {
        final Drawable defaultUserIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final String userIconUri = getUserIconUri(user, context);
        if (!StringUtils.isEmpty(userIconUri)) {
            MessengerConfigurationImpl.getInstance().getServiceLocator().getRemoteFileService().loadImage(userIconUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultUserIcon);
        }
    }

    public void fetchUserIcon(@NotNull User user, @NotNull Context context) {
        final String userIconUri = getUserIconUri(user, context);
        if (!StringUtils.isEmpty(userIconUri)) {
            MessengerConfigurationImpl.getInstance().getServiceLocator().getRemoteFileService().loadImage(userIconUri);
        }
    }

    public void fetchFriendsIcons(@NotNull User user, @NotNull Context context) {
        for (User friend : getUserFriends(user.getId(), context)) {
            fetchUserIcon(friend, context);
        }
    }

    @Nullable
    private String getUserIconUri(@NotNull User user, @NotNull Context context) {
        return user.getPropertyValueByName("photo");
    }

    @NotNull
    private UserDao getUserDao(@NotNull Context context) {
        return MessengerConfigurationImpl.getInstance().getDaoLocator().getUserDao(context);
    }

    @NotNull
    private ApiUserService getApiUserService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getApiUserService();
    }

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    @Override
    public void addUserEventListener(@NotNull UserEventListener userEventListener) {
        listeners.addUserEventListener(userEventListener);
    }

    @Override
    public void removeUserEventListener(@NotNull UserEventListener userEventListener) {
        listeners.removeUserEventListener(userEventListener);
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

        synchronized (userFriendsCache) {

            if (userEventType == UserEventType.changed) {
                // user changed => update it in friends cache
                for (List<User> friends : userFriendsCache.values()) {
                    for (int i = 0; i < friends.size(); i++) {
                        final User friend = friends.get(i);
                        if (friend.equals(eventUser)) {
                            friends.set(i, eventUser);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.friend_added) {
                // friend added => need to add to list of cached friends
                if (data instanceof User) {
                    final User friend = ((User) data);
                    final List<User> friends = userFriendsCache.get(eventUser.getId());
                    if (friends != null) {
                        // check if not contains as can be added in parallel
                        if (!Iterables.contains(friends, friend)) {
                            friends.add(friend);
                        }
                    }
                }
            }

            if (userEventType == UserEventType.friend_added_batch) {
                // friends added => need to add to list of cached friends
                if (data instanceof List) {
                    final List<User> friends = (List<User>) data;
                    final List<User> friendsFromCache = userFriendsCache.get(eventUser.getId());
                    if (friendsFromCache != null) {
                        for (User friend : friends) {
                            // check if not contains as can be added in parallel
                            if (!Iterables.contains(friendsFromCache, friend)) {
                                friendsFromCache.add(friend);
                            }
                        }
                    }
                }
            }

            if (userEventType == UserEventType.friend_removed) {
                // friend removed => try to remove from cached friends
                if (data instanceof User) {
                    final User friend = ((User) data);
                    final List<User> friends = userFriendsCache.get(eventUser.getId());
                    if (friends != null) {
                        friends.remove(friend);
                    }
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
                if (data instanceof List) {
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
            }

            if (userEventType == UserEventType.chat_removed) {
                if (data instanceof Chat) {
                    final Chat chat = ((Chat) data);
                    final List<Chat> chats = userChatsCache.get(eventUser.getId());
                    if (chats != null) {
                        chats.remove(chat);
                    }
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
