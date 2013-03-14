package org.solovyev.android.messenger.chats;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;
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
 * Date: 6/6/12
 * Time: 2:43 AM
 */
@Singleton
public class DefaultChatService implements ChatService {

    @Nonnull
    private static final Character PRIVATE_CHAT_DELIMITER = ':';

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
    private ChatDao chatDao;

    @Inject
    @Nonnull
    private ChatMessageService chatMessageService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ImageLoader imageLoader;

    @Inject
    @Nonnull
    private Application context;

    @Inject
    @Nonnull
    private ChatMessageDao chatMessageDao;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @Nonnull
    private static final String EVENT_TAG = "ChatEvent";

    @Nonnull
    private final JEventListeners<JEventListener<? extends ChatEvent>, ChatEvent> listeners = Listeners.newEventListenersBuilderFor(ChatEvent.class).withHardReferences().onCallerThread().create();

    // key: chat id, value: list of participants
    @Nonnull
    private final Map<Entity, List<User>> chatParticipantsCache = new HashMap<Entity, List<User>>();

    // key: chat id, value: last message
    @Nonnull
    private final Map<Entity, ChatMessage> lastMessagesCache = new HashMap<Entity, ChatMessage>();

    // key: chat id, value: chat
    @Nonnull
    private final Map<Entity, Chat> chatsById = new HashMap<Entity, Chat>();

    @Nonnull
    private final Object lock = new Object();

    public DefaultChatService() {
        listeners.addListener(new ChatEventListener());
    }

    @Override
    public void init() {
        userService.addListener(new UserEventListener());
    }

    @Nonnull
    @Override
    public Chat updateChat(@Nonnull Chat chat) {
        synchronized (lock) {
            getChatDao().updateChat(chat);
        }

        fireEvent(ChatEventType.changed.newEvent(chat, null));

        return chat;
    }

    @Nonnull
    @Override
    public Chat newPrivateChat(@Nonnull Entity realmUser1, @Nonnull Entity realmUser2) {
        final Realm realm = getRealmByUser(realmUser1);
        final RealmChatService realmChatService = realm.getRealmChatService();

        Chat result;

        final Entity realmChat = newPrivateChatId(realmUser1, realmUser2);
        synchronized (lock) {
            result = getChatById(realmChat);
            if ( result == null ) {
                Chat chat = realmChatService.newPrivateChat(realmChat, realmUser1.getRealmEntityId(), realmUser2.getRealmEntityId());
                chat = preparePrivateChat(chat, realmUser1, realmUser2);

                final List<User> participants = new ArrayList<User>(2);
                participants.add(getUserService().getUserById(realmUser1));
                participants.add(getUserService().getUserById(realmUser2));
                final ApiChat apiChat = Chats.newEmptyApiChat(chat, participants);

                getUserService().mergeUserChats(realmUser1, Arrays.asList(apiChat));

                result = apiChat.getChat();
            }
        }

        return result;
    }

    @Nonnull
    private Chat preparePrivateChat(@Nonnull Chat chat, @Nonnull Entity realmUser1, @Nonnull Entity realmUser2) {
        final Realm realm = getRealmByUser(realmUser1);
        final Entity realmChat = newPrivateChatId(realmUser1, realmUser2);

        if (!realmChat.getRealmEntityId().equals(chat.getEntity().getRealmEntityId())) {
            /**
             * chat id that was created by realm (may differ from one created in {@link org.solovyev.android.messenger.chats.ChatService#newPrivateChatId(org.solovyev.android.messenger.entities.Entity, org.solovyev.android.messenger.entities.Entity)) method)
             */
            final String realmChatId = chat.getEntity().getRealmEntityId();

            // copy with new id
            chat = chat.copyWithNew(realm.newRealmEntity(realmChatId, realmChat.getEntityId()));
        }

        return chat;
    }

    @Nonnull
    private ApiChat prepareChat(@Nonnull ApiChat apiChat) {
        if (apiChat.getChat().isPrivate()) {
            final Realm realm = realmService.getRealmById(apiChat.getChat().getEntity().getRealmId());
            final User user = realm.getUser();
            final List<User> participants = apiChat.getParticipantsExcept(user);

            if (participants.size() == 1) {
                final Entity realmUser1 = user.getEntity();
                final Entity realmUser2 = participants.get(0).getEntity();

                final Entity realmChat = newPrivateChatId(realmUser1, realmUser2);

                if (!realmChat.getRealmEntityId().equals(apiChat.getChat().getEntity().getRealmEntityId())) {
                    /**
                     * chat id that was created by realm (may differ from one created in {@link org.solovyev.android.messenger.chats.ChatService#newPrivateChatId(org.solovyev.android.messenger.entities.Entity, org.solovyev.android.messenger.entities.Entity)) method)
                     */
                    final String realmChatId = apiChat.getChat().getEntity().getRealmEntityId();

                    // copy with new id
                    apiChat = apiChat.copyWithNew(realm.newRealmEntity(realmChatId, realmChat.getEntityId()));
                }
            }
        }

        return apiChat;
    }

    @Nonnull
    @Override
    public List<Chat> loadUserChats(@Nonnull Entity user) {
        return getChatDao().loadUserChats(user.getEntityId());
    }

    @Nonnull
    @Override
    public ApiChat saveChat(@Nonnull Entity realmUser, @Nonnull ApiChat chat) {
        final MergeDaoResult<ApiChat, String> result = mergeUserChats(realmUser.getEntityId(), Arrays.asList(chat));
        if ( result.getAddedObjects().size() > 0 ) {
            return result.getAddedObjects().get(0);
        } else if (result.getUpdatedObjects().size() > 0) {
            return result.getUpdatedObjects().get(0);
        } else {
            return chat;
        }
    }

    @Nonnull
    @Override
    public MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats) {
        synchronized (lock) {
            final List<ApiChat> preparedChats = Lists.transform(chats, new Function<ApiChat, ApiChat>() {
                @Override
                public ApiChat apply(@Nullable ApiChat chat) {
                    assert chat != null;
                    return prepareChat(chat);
                }
            });
            return getChatDao().mergeUserChats(userId, preparedChats);
        }
    }

    @Override
    public Chat getChatById(@Nonnull Entity realmChat) {
        Chat result;

        synchronized (chatsById) {
            result = chatsById.get(realmChat);
        }

        if (result == null) {
            synchronized (lock) {
                result = getChatDao().loadChatById(realmChat.getEntityId());
            }

            if ( result != null ) {
                synchronized (chatsById) {
                    chatsById.put(result.getEntity(), result);
                }
            }
        }

        return result;
    }


    @Nonnull
    private Realm getRealmByUser(@Nonnull Entity realmUser) {
        return realmService.getRealmById(realmUser.getRealmId());
    }

    @Nonnull
    @Override
    public List<ChatMessage> syncChatMessages(@Nonnull Entity user) {
        final List<ChatMessage> chatMessages = getRealmByUser(user).getRealmChatService().getChatMessages(user.getRealmEntityId());

/*        synchronized (userChatsCache) {
            userChatsCache.put(userId, chats);
        }

        User user = this.getUserById(userId, context);
        final MergeDaoResult<Chat, String> result;
        synchronized (lock) {
            result = getChatService().updateUserChats(userId, chats, context);

            // update sync data
            user = user.updateChatsSyncDate();
            updateUser(user, context);
        }

        final List<UserEventContainer.UserEvent> userEvents = new ArrayList<UserEventContainer.UserEvent>(chats.size());
        final List<ChatEventContainer.ChatEvent> chatEvents = new ArrayList<ChatEventContainer.ChatEvent>(chats.size());

        for (Chat addedChatLink : result.getAddedObjectLinks()) {
            userEvents.add(new UserEventContainer.UserEvent(user, UserEventType.chat_added, addedChatLink));
        }

        for (Chat addedChat : result.getAddedObjects()) {
            chatEvents.add(new ChatEventContainer.ChatEvent(addedChat, ChatEventType.added, null));
            userEvents.add(new UserEventContainer.UserEvent(user, UserEventType.chat_added, addedChat));
        }

        for (String removedChatId : result.getRemovedObjectIds()) {
            userEvents.add(new UserEventContainer.UserEvent(user, UserEventType.chat_removed, removedChatId));
        }

        for (Chat updatedChat : result.getUpdatedObjects()) {
            chatEvents.add(new ChatEventContainer.ChatEvent(updatedChat, ChatEventType.changed, null));
        }

        listeners.fireEvents(userEvents);
        getChatService().fireChatEvents(chatEvents);*/

        return java.util.Collections.unmodifiableList(chatMessages);
    }

    @Nonnull
    @Override
    public List<ChatMessage> syncNewerChatMessagesForChat(@Nonnull Entity realmChat, @Nonnull Entity realmUser) {
        final Realm realm = getRealmByUser(realmUser);
        final RealmChatService realmChatService = realm.getRealmChatService();

        final List<ChatMessage> messages = realmChatService.getNewerChatMessagesForChat(realmChat.getRealmEntityId(), realmUser.getRealmEntityId());

        saveChatMessages(realmChat, messages, true);

        return java.util.Collections.unmodifiableList(messages);

    }

    @Override
    public void saveChatMessages(@Nonnull Entity realmChat, @Nonnull List<? extends ChatMessage> messages, boolean updateChatSyncDate) {
        Chat chat = this.getChatById(realmChat);

        if (chat != null) {
            final MergeDaoResult<ChatMessage, String> result;
            synchronized (lock) {
                result = getChatMessageDao().mergeChatMessages(realmChat.getEntityId(), messages, false);

                // update sync data
                if (updateChatSyncDate) {
                    chat = chat.updateMessagesSyncDate();
                    updateChat(chat);
                }
            }

            final List<ChatEvent> chatEvents = new ArrayList<ChatEvent>(messages.size());

            chatEvents.add(ChatEventType.message_added_batch.newEvent(chat, result.getAddedObjects()));

            // cannot to remove as not all message can be loaded
/*            for (Integer removedMessageId : result.getRemovedObjectIds()) {
                chatEvents.add(new ChatEvent(chat, ChatEventType.message_removed, removedMessageId));
            }*/

            for (ChatMessage updatedMessage : result.getUpdatedObjects()) {
                chatEvents.add(ChatEventType.message_changed.newEvent(chat, updatedMessage));
            }

            fireEvents(chatEvents);
        } else {
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + realmChat.getEntityId());
        }
    }

    @Nonnull
    private ChatMessageDao getChatMessageDao() {
        return chatMessageDao;
    }

    @Nonnull
    @Override
    public List<ChatMessage> syncOlderChatMessagesForChat(@Nonnull Entity realmChat, @Nonnull Entity realmUser) {
        final Integer offset = getChatMessageService().getChatMessages(realmChat).size();

        final Chat chat = this.getChatById(realmChat);

        final List<ChatMessage> messages;

        if (chat != null) {
            messages = getRealmByUser(realmUser).getRealmChatService().getOlderChatMessagesForChat(realmChat.getRealmEntityId(), realmUser.getRealmEntityId(), offset);
            saveChatMessages(realmChat, messages, false);
        } else {
            messages = java.util.Collections.emptyList();
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + realmChat.getEntityId());
        }

        return java.util.Collections.unmodifiableList(messages);
    }

    @Override
    public void syncChat(@Nonnull Entity realmChat, @Nonnull Entity realmUser) {
        // todo serso: check if OK
        syncNewerChatMessagesForChat(realmChat, realmUser);
    }

    @Nullable
    @Override
    public Entity getSecondUser(@Nonnull Chat chat) {
        boolean first = true;

        for (String userId : Splitter.on(PRIVATE_CHAT_DELIMITER).split(chat.getEntity().getAppRealmEntityId())) {
            if ( first ) {
                first = false;
            } else {
                return EntityImpl.newInstance(chat.getEntity().getRealmId(), userId);
            }
        }

        return null;
    }

    @Override
    public void setChatIcon(@Nonnull ImageView imageView, @Nonnull Chat chat, @Nonnull User user) {
        final Drawable defaultChatIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final List<User> otherParticipants = this.getParticipantsExcept(chat.getEntity(), user.getEntity());

        final String imageUri;
        if (!otherParticipants.isEmpty()) {
            final User participant = otherParticipants.get(0);
            imageUri = participant.getPropertyValueByName("photo");
        } else {
            imageUri = null;
        }

        if (!Strings.isEmpty(imageUri)) {
            this.imageLoader.loadImage(imageUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultChatIcon);
        }
    }

    @Nonnull
    @Override
    public Entity newPrivateChatId(@Nonnull Entity realmUser1, @Nonnull Entity realmUser2) {
        return getRealmByUser(realmUser1).newRealmEntity(realmUser1.getRealmEntityId() + PRIVATE_CHAT_DELIMITER + realmUser2.getRealmEntityId());
    }

    @Nonnull
    private ChatMessageService getChatMessageService() {
        return this.chatMessageService;
    }

    @Nonnull
    @Override
    public Chat getPrivateChat(@Nonnull Entity user1, @Nonnull final Entity user2) {
        final Entity realmChat = this.newPrivateChatId(user1, user2);

        Chat result = this.getChatById(realmChat);
        if (result == null) {
            result = this.newPrivateChat(user1, user2);
        }

        return result;
    }

    @Nonnull
    @Override
    public List<User> getParticipants(@Nonnull Entity realmChat) {
        List<User> result;

        synchronized (chatParticipantsCache) {
            result = chatParticipantsCache.get(realmChat);
            if (result == null) {
                result = getChatDao().loadChatParticipants(realmChat.getEntityId());
                if (!Collections.isEmpty(result)) {
                    chatParticipantsCache.put(realmChat, result);
                }
            }
        }

        // result list might be in cache and might be updated due to some events => must COPY
        return new ArrayList<User>(result);
    }

    @Nonnull
    @Override
    public List<User> getParticipantsExcept(@Nonnull Entity realmChat, @Nonnull final Entity realmUser) {
        final List<User> participants = getParticipants(realmChat);
        return Lists.newArrayList(Iterables.filter(participants, new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User input) {
                return input != null && !input.getEntity().equals(realmUser);
            }
        }));
    }

    @Nullable
    @Override
    public ChatMessage getLastMessage(@Nonnull Entity realmChat) {
        ChatMessage result;

        synchronized (lastMessagesCache) {
            result = lastMessagesCache.get(realmChat);
            if (result == null) {
                result = getChatMessageDao().loadLastChatMessage(realmChat.getEntityId());
                if (result != null) {
                    lastMessagesCache.put(realmChat, result);
                }
            }
        }

        return result;
    }

    @Nonnull
    private UserService getUserService() {
        return this.userService;
    }

    @Nonnull
    private ChatDao getChatDao() {
        return chatDao;
    }

    @Override
    public boolean addListener(@Nonnull JEventListener<ChatEvent> listener) {
        return this.listeners.addListener(listener);
    }

    @Override
    public boolean removeListener(@Nonnull JEventListener<ChatEvent> listener) {
        return this.listeners.removeListener(listener);
    }

    @Override
    public void fireEvent(@Nonnull ChatEvent event) {
        this.listeners.fireEvent(event);
    }

    @Override
    public void fireEvents(@Nonnull Collection<ChatEvent> events) {
        this.listeners.fireEvents(events);
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
            synchronized (chatParticipantsCache) {
                final User eventUser = event.getUser();

                if (event.getType() == UserEventType.changed) {
                    for (List<User> participants : chatParticipantsCache.values()) {
                        for (int i = 0; i < participants.size(); i++) {
                            final User participant = participants.get(i);
                            if (participant.equals(eventUser)) {
                                participants.set(i, eventUser);
                            }
                        }
                    }
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
            final Chat eventChat = event.getChat();
            final ChatEventType type = event.getType();
            final Object data = event.getData();

            synchronized (chatParticipantsCache) {

                if (type == ChatEventType.participant_added) {
                    // participant added => need to add to list of cached participants
                    if (data instanceof User) {
                        final User participant = ((User) data);
                        final List<User> participants = chatParticipantsCache.get(eventChat.getEntity());
                        if (participants != null) {
                            // check if not contains as can be added in parallel
                            if (!Iterables.contains(participants, participant)) {
                                participants.add(participant);
                            }
                        }
                    }
                }

                if (type == ChatEventType.participant_removed) {
                    // participant removed => try to remove from cached participants
                    if (data instanceof User) {
                        final User participant = ((User) data);
                        final List<User> participants = chatParticipantsCache.get(eventChat.getEntity());
                        if (participants != null) {
                            participants.remove(participant);
                        }
                    }
                }
            }

            synchronized (chatsById) {
                if (event.isOfType(ChatEventType.changed, ChatEventType.changed, ChatEventType.last_message_changed)) {
                    chatsById.put(eventChat.getEntity(), eventChat);
                }
            }


            final Map<Chat, ChatMessage> changesLastMessages = new HashMap<Chat, ChatMessage>();
            synchronized (lastMessagesCache) {

                if (type == ChatEventType.message_added) {
                    if (data instanceof ChatMessage) {
                        final ChatMessage message = (ChatMessage) data;
                        final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getEntity());
                        if (messageFromCache == null || message.getSendDate().isAfter(messageFromCache.getSendDate()) ) {
                            lastMessagesCache.put(eventChat.getEntity(), message);
                            changesLastMessages.put(eventChat, message);
                        }
                    }
                }

                if (type == ChatEventType.message_added_batch) {
                    if (data instanceof List) {
                        final List<ChatMessage> messages = (List<ChatMessage>) data;

                        ChatMessage newestMessage = null;
                        for (ChatMessage message : messages) {
                            if (newestMessage == null) {
                                newestMessage = message;
                            } else if (message.getSendDate().isAfter(newestMessage.getSendDate())) {
                                newestMessage = message;
                            }
                        }

                        final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getEntity());
                        if (newestMessage != null && (messageFromCache == null || newestMessage.getSendDate().isAfter(messageFromCache.getSendDate()))) {
                            lastMessagesCache.put(eventChat.getEntity(), newestMessage);
                            changesLastMessages.put(eventChat, newestMessage);
                        }
                    }
                }


                if (type == ChatEventType.message_changed) {
                    if (data instanceof ChatMessage) {
                        final ChatMessage message = (ChatMessage) data;
                        final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getEntity());
                        if (messageFromCache == null || messageFromCache.equals(message)) {
                            lastMessagesCache.put(eventChat.getEntity(), message);
                            changesLastMessages.put(eventChat, message);
                        }
                    }
                }

            }

            for (Map.Entry<Chat, ChatMessage> changedLastMessageEntry : changesLastMessages.entrySet()) {
                ChatEventType.last_message_changed.newEvent(changedLastMessageEntry.getKey(), changedLastMessageEntry.getValue());
            }
        }
    }
}
