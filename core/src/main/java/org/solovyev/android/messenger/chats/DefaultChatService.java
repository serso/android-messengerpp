package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.roboguice.RoboGuiceUtils;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.listeners.JListeners;
import org.solovyev.common.listeners.Listeners;
import org.solovyev.common.text.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:43 AM
 */
@Singleton
public class DefaultChatService implements ChatService, ChatEventListener, UserEventListener {

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
    private Provider<ChatMessageDao> chatMessageDaoProvider;

    @Inject
    @NotNull
    private Provider<ChatDao> chatDaoProvider;

    @Inject
    @NotNull
    private ChatMessageService chatMessageService;

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private ImageLoader imageLoader;
    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @NotNull
    private static final String EVENT_TAG = "ChatEvent";

    @NotNull
    private final JListeners<ChatEventListener> listeners = Listeners.newWeakRefListeners();

    // key: chat id, value: list of participants
    @NotNull
    private final Map<String, List<User>> chatParticipantsCache = new HashMap<String, List<User>>();

    // key: chat id, value: last message
    @NotNull
    private final Map<String, ChatMessage> lastMessagesCache = new HashMap<String, ChatMessage>();

    // key: chat id, value: chat
    @NotNull
    private final Map<String, Chat> chatsById = new HashMap<String, Chat>();

    @NotNull
    private final Object lock = new Object();

    public DefaultChatService() {
        listeners.addListener(this);
    }

    @Override
    public void init() {
        userService.addListener(this);
    }

    @NotNull
    @Override
    public Chat updateChat(@NotNull Chat chat, @NotNull Context context) {
        synchronized (lock) {
            getChatDao(context).updateChat(chat);
        }

        fireChatEvent(chat, ChatEventType.changed, null);

        return chat;
    }

    @NotNull
    @Override
    public Chat createPrivateChat(@NotNull RealmEntity realmUser, @NotNull RealmEntity secondRealmUser, @NotNull Context context) {
        Chat result;

        final String chatId = createPrivateChatId(realmUser, secondRealmUser);
        synchronized (lock) {
            result = getChatById(chatId, context);
            if ( result == null ) {
                final ApiChatImpl apiChat = ApiChatImpl.newInstance(RealmEntityImpl.fromEntityId(chatId), 0, true);
                apiChat.addParticipant(getUserService().getUserById(realmUser.getEntityId(), context));
                apiChat.addParticipant(getUserService().getUserById(secondRealmUser.getEntityId(), context));

                getUserService().mergeUserChats(realmUser.getEntityId(), Arrays.asList(apiChat), context);

                result = apiChat.getChat();
            }
        }

        return result;
    }

    @NotNull
    @Override
    public List<Chat> loadUserChats(@NotNull String userId, @NotNull Context context) {
        return getChatDao(context).loadUserChats(userId);
    }

    @NotNull
    @Override
    public MergeDaoResult<ApiChat, String> mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> chats, @NotNull Context context) {
        synchronized (lock) {
            return getChatDao(context).mergeUserChats(userId, chats);
        }
    }

    @Override
    public Chat getChatById(@NotNull String chatId, @NotNull Context context) {
        Chat result;

        synchronized (chatsById) {
            result = chatsById.get(chatId);
        }

        if (result == null) {
            synchronized (lock) {
                result = getChatDao(context).loadChatById(chatId);
            }

            if ( result != null ) {
                synchronized (chatsById) {
                    chatsById.put(result.getId(), result);
                }
            }
        }

        return result;
    }

    @NotNull
    @Override
    public List<ChatMessage> syncChatMessages(@NotNull String userId, @NotNull Context context) {
        final List<ChatMessage> chatMessages = realm.getRealmChatService().getChatMessages(userId, context);

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

        listeners.fireUserEvents(userEvents);
        getChatService().fireChatEvents(chatEvents);*/

        return java.util.Collections.unmodifiableList(chatMessages);
    }

    @NotNull
    @Override
    public List<ChatMessage> syncNewerChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context) {
        final List<ChatMessage> messages = realm.getRealmChatService().getNewerChatMessagesForChat(chatId, userId, context);

        syncChatMessagesForChat(chatId, context, messages);

        return java.util.Collections.unmodifiableList(messages);

    }

    private void syncChatMessagesForChat(@NotNull String chatId, @NotNull Context context, @NotNull List<ChatMessage> messages) {
        Chat chat = this.getChatById(chatId, context);

        if (chat != null) {
            final MergeDaoResult<ChatMessage, String> result;
            synchronized (lock) {
                result = getChatMessageDao(context).mergeChatMessages(chatId, messages, false, context);

                // update sync data
                chat = chat.updateMessagesSyncDate();
                updateChat(chat, context);
            }

            final List<ChatEvent> chatEvents = new ArrayList<ChatEvent>(messages.size());

            chatEvents.add(new ChatEvent(chat, ChatEventType.message_added_batch, result.getAddedObjects()));

            // cannot to remove as not all message can be loaded
/*            for (Integer removedMessageId : result.getRemovedObjectIds()) {
                chatEvents.add(new ChatEvent(chat, ChatEventType.message_removed, removedMessageId));
            }*/

            for (ChatMessage updatedMessage : result.getUpdatedObjects()) {
                chatEvents.add(new ChatEvent(chat, ChatEventType.message_changed, updatedMessage));
            }

            fireChatEvents(chatEvents);
        } else {
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + chatId);
        }
    }

    @NotNull
    private ChatMessageDao getChatMessageDao(@NotNull Context context) {
        return RoboGuiceUtils.getInContextScope(context, chatMessageDaoProvider);
    }

    @NotNull
    @Override
    public List<ChatMessage> syncOlderChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context) {
        final Integer offset = getChatMessageService().getChatMessages(chatId, context).size();

        final Chat chat = this.getChatById(chatId, context);

        final List<ChatMessage> messages;

        if (chat != null) {
            messages = realm.getRealmChatService().getOlderChatMessagesForChat(chatId, userId, offset, context);
            syncChatMessagesForChat(chatId, context, messages);
        } else {
            messages = java.util.Collections.emptyList();
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + chatId);
        }

        return java.util.Collections.unmodifiableList(messages);
    }

    @Override
    public void syncChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context) {
        // todo serso: check if OK
        syncNewerChatMessagesForChat(chatId, userId, context);
    }

    @Nullable
    @Override
    public String getSecondUserId(@NotNull Chat chat) {
        boolean first = true;
        for (String userId : Splitter.on('_').split(chat.getId())) {
            if ( first ) {
                first = false;
            } else {
                return userId;
            }
        }

        return null;
    }

    @Override
    public void setChatIcon(@NotNull ImageView imageView, @NotNull Chat chat, @NotNull User user, @NotNull Context context) {
        final Drawable defaultChatIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final List<User> otherParticipants = this.getParticipantsExcept(chat.getId(), user.getId(), context);

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

    @NotNull
    @Override
    public String createPrivateChatId(@NotNull RealmEntity realmUser, @NotNull RealmEntity secondRealmUser) {
        return RealmEntityImpl.newInstance(realmUser.getRealmId(), realmUser.getRealmEntityId() + "_" + secondRealmUser.getRealmEntityId()).getEntityId();
    }

    @NotNull
    @Override
    public ChatMessage sendChatMessage(@NotNull String userId, @NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context) {
        final String chatMessageId = realm.getRealmChatService().sendChatMessage(chat, chatMessage, context);

        final LiteChatMessageImpl msgResult = LiteChatMessageImpl.newInstance(chatMessageId);

        msgResult.setAuthor(getUserService().getUserById(userId, context));
        if ( chat.isPrivate() ) {
            final String secondUserId = chat.getSecondUserId();
            msgResult.setRecipient(getUserService().getUserById(secondUserId, context));
        }
        msgResult.setBody(chatMessage.getBody());
        msgResult.setTitle(chatMessage.getTitle());
        msgResult.setSendDate(DateTime.now());

        final ChatMessageImpl result = new ChatMessageImpl(msgResult);
        for (LiteChatMessage fwtMessage : chatMessage.getFwdMessages()) {
            result.addFwdMessage(fwtMessage);
        }

        result.setDirection(MessageDirection.out);
        result.setRead(true);

        return result;
    }

    @NotNull
    private ChatMessageService getChatMessageService() {
        return this.chatMessageService;
    }

    @NotNull
    @Override
    public List<User> getParticipants(@NotNull String chatId, @NotNull Context context) {
        List<User> result;

        synchronized (chatParticipantsCache) {
            result = chatParticipantsCache.get(chatId);
            if (result == null) {
                result = getChatDao(context).loadChatParticipants(chatId);
                if (!Collections.isEmpty(result)) {
                    chatParticipantsCache.put(chatId, result);
                }
            }
        }

        // result list might be in cache and might be updated due to some events => must COPY
        return new ArrayList<User>(result);
    }

    @NotNull
    @Override
    public List<User> getParticipantsExcept(@NotNull String chatId, @NotNull final String userId, @NotNull Context context) {
        final List<User> participants = getParticipants(chatId, context);
        return Lists.newArrayList(Iterables.filter(participants, new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User input) {
                return input != null && !input.getId().equals(userId);
            }
        }));
    }

    @Nullable
    @Override
    public ChatMessage getLastMessage(@NotNull String chatId, @NotNull Context context) {
        ChatMessage result;

        synchronized (lastMessagesCache) {
            result = lastMessagesCache.get(chatId);
            if (result == null) {
                result = getChatMessageDao(context).loadLastChatMessage(chatId);
                if (result != null) {
                    lastMessagesCache.put(chatId, result);
                }
            }
        }

        return result;
    }

    @NotNull
    private UserService getUserService() {
        return this.userService;
    }

    @NotNull
    private ChatDao getChatDao(@NotNull Context context) {
        return RoboGuiceUtils.getInContextScope(context, chatDaoProvider);
    }

    @Override
    public void addChatEventListener(@NotNull ChatEventListener chatEventListener) {
        this.listeners.addListener(chatEventListener);
    }

    @Override
    public void removeChatEventListener(@NotNull ChatEventListener chatEventListener) {
        this.listeners.removeListener(chatEventListener);
    }

    @Override
    public void fireChatEvent(@NotNull Chat chat, @NotNull ChatEventType chatEventType, @Nullable Object data) {
        fireChatEvents(Arrays.asList(new ChatEvent(chat, chatEventType, data)));
    }

    @Override
    public void fireChatEvents(@NotNull List<ChatEvent> chatEvents) {
        final Collection<ChatEventListener> listeners = this.listeners.getListeners();
        for (ChatEvent chatEvent : chatEvents) {
            Log.d(EVENT_TAG, "Event: " + chatEvent.getChatEventType() + " for chat: " + chatEvent.getChat().getId() + " with data: " + chatEvent.getData());
            for (ChatEventListener listener : listeners) {
                listener.onChatEvent(chatEvent.getChat(), chatEvent.getChatEventType(), chatEvent.getData());
            }
        }
    }

    @Override
    public void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data) {
        synchronized (chatParticipantsCache) {

            if (chatEventType == ChatEventType.participant_added) {
                // participant added => need to add to list of cached participants
                if (data instanceof User) {
                    final User participant = ((User) data);
                    final List<User> participants = chatParticipantsCache.get(eventChat.getId());
                    if (participants != null) {
                        // check if not contains as can be added in parallel
                        if (!Iterables.contains(participants, participant)) {
                            participants.add(participant);
                        }
                    }
                }
            }

            if (chatEventType == ChatEventType.participant_removed) {
                // participant removed => try to remove from cached participants
                if (data instanceof User) {
                    final User participant = ((User) data);
                    final List<User> participants = chatParticipantsCache.get(eventChat.getId());
                    if (participants != null) {
                        participants.remove(participant);
                    }
                }
            }
        }

        synchronized (chatsById) {
            if ( chatEventType == ChatEventType.changed || chatEventType == ChatEventType.added || chatEventType == ChatEventType.last_message_changed ) {
                chatsById.put(eventChat.getId(), eventChat);
            }
        }


        final Map<Chat, ChatMessage> changesLastMessages = new HashMap<Chat, ChatMessage>();
        synchronized (lastMessagesCache) {

            if (chatEventType == ChatEventType.message_added) {
                if (data instanceof ChatMessage) {
                    final ChatMessage message = (ChatMessage) data;
                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getId());
                    if (messageFromCache == null || message.getSendDate().isAfter(messageFromCache.getSendDate()) ) {
                        lastMessagesCache.put(eventChat.getId(), message);
                        changesLastMessages.put(eventChat, message);
                    }
                }
            }

            if (chatEventType == ChatEventType.message_added_batch) {
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

                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getId());
                    if (newestMessage != null && (messageFromCache == null || newestMessage.getSendDate().isAfter(messageFromCache.getSendDate()))) {
                        lastMessagesCache.put(eventChat.getId(), newestMessage);
                        changesLastMessages.put(eventChat, newestMessage);
                    }
                }
            }


            if (chatEventType == ChatEventType.message_changed) {
                if (data instanceof ChatMessage) {
                    final ChatMessage message = (ChatMessage) data;
                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getId());
                    if (messageFromCache == null || messageFromCache.equals(message)) {
                        lastMessagesCache.put(eventChat.getId(), message);
                        changesLastMessages.put(eventChat, message);
                    }
                }
            }

        }

        for (Map.Entry<Chat, ChatMessage> changedLastMessageEntry : changesLastMessages.entrySet()) {
            fireChatEvent(changedLastMessageEntry.getKey(), ChatEventType.last_message_changed, changedLastMessageEntry.getValue());
        }
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        synchronized (chatParticipantsCache) {

            if (userEventType == UserEventType.changed) {
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
