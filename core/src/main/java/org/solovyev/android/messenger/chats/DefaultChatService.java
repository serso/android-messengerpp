package org.solovyev.android.messenger.chats;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmService;
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
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private Provider<ChatMessageDao> chatMessageDaoProvider;

    @Inject
    @Nonnull
    private Provider<ChatDao> chatDaoProvider;

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
    private final JListeners<ChatEventListener> listeners = Listeners.newWeakRefListeners();

    // key: chat id, value: list of participants
    @Nonnull
    private final Map<RealmEntity, List<User>> chatParticipantsCache = new HashMap<RealmEntity, List<User>>();

    // key: chat id, value: last message
    @Nonnull
    private final Map<RealmEntity, ChatMessage> lastMessagesCache = new HashMap<RealmEntity, ChatMessage>();

    // key: chat id, value: chat
    @Nonnull
    private final Map<RealmEntity, Chat> chatsById = new HashMap<RealmEntity, Chat>();

    @Nonnull
    private final Object lock = new Object();

    public DefaultChatService() {
        listeners.addListener(this);
    }

    @Override
    public void init() {
        userService.addListener(this);
    }

    @Nonnull
    @Override
    public Chat updateChat(@Nonnull Chat chat) {
        synchronized (lock) {
            getChatDao(context).updateChat(chat);
        }

        fireChatEvent(chat, ChatEventType.changed, null);

        return chat;
    }

    @Nonnull
    @Override
    public Chat createPrivateChat(@Nonnull RealmEntity user, @Nonnull RealmEntity secondRealmUser) {
        Chat result;

        final RealmEntity realmChat = createPrivateChatId(user, secondRealmUser);
        synchronized (lock) {
            result = getChatById(realmChat);
            if ( result == null ) {
                final ApiChatImpl apiChat = ApiChatImpl.newInstance(realmChat, 0, true);
                apiChat.addParticipant(getUserService().getUserById(user));
                apiChat.addParticipant(getUserService().getUserById(secondRealmUser));

                getUserService().mergeUserChats(user, Arrays.asList(apiChat));

                result = apiChat.getChat();
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public List<Chat> loadUserChats(@Nonnull RealmEntity user) {
        return getChatDao(context).loadUserChats(user.getEntityId());
    }

    @Nonnull
    @Override
    public MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats) {
        synchronized (lock) {
            return getChatDao(context).mergeUserChats(userId, chats);
        }
    }

    @Override
    public Chat getChatById(@Nonnull RealmEntity realmChat) {
        Chat result;

        synchronized (chatsById) {
            result = chatsById.get(realmChat);
        }

        if (result == null) {
            synchronized (lock) {
                result = getChatDao(context).loadChatById(realmChat.getEntityId());
            }

            if ( result != null ) {
                synchronized (chatsById) {
                    chatsById.put(result.getRealmChat(), result);
                }
            }
        }

        return result;
    }


    @Nonnull
    private Realm getRealmByUser(@Nonnull RealmEntity realmUser) {
        return realmService.getRealmById(realmUser.getRealmId());
    }

    @Nonnull
    @Override
    public List<ChatMessage> syncChatMessages(@Nonnull RealmEntity user) {
        final List<ChatMessage> chatMessages = getRealmByUser(user).getRealmChatService().getChatMessages(user.getRealmEntityId(), context);

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

    @Nonnull
    @Override
    public List<ChatMessage> syncNewerChatMessagesForChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser) {
        final List<ChatMessage> messages = getRealmByUser(realmUser).getRealmChatService().getNewerChatMessagesForChat(realmChat.getRealmEntityId(), realmUser.getRealmEntityId(), context);

        syncChatMessagesForChat(realmChat, context, messages);

        return java.util.Collections.unmodifiableList(messages);

    }

    private void syncChatMessagesForChat(@Nonnull RealmEntity realmChat, @Nonnull Context context, @Nonnull List<ChatMessage> messages) {
        Chat chat = this.getChatById(realmChat);

        if (chat != null) {
            final MergeDaoResult<ChatMessage, String> result;
            synchronized (lock) {
                result = getChatMessageDao(context).mergeChatMessages(realmChat.getEntityId(), messages, false, context);

                // update sync data
                chat = chat.updateMessagesSyncDate();
                updateChat(chat);
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
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + realmChat.getEntityId());
        }
    }

    @Nonnull
    private ChatMessageDao getChatMessageDao(@Nonnull Context context) {
        return RoboGuiceUtils.getInContextScope(context, chatMessageDaoProvider);
    }

    @Nonnull
    @Override
    public List<ChatMessage> syncOlderChatMessagesForChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser) {
        final Integer offset = getChatMessageService().getChatMessages(realmChat, context).size();

        final Chat chat = this.getChatById(realmChat);

        final List<ChatMessage> messages;

        if (chat != null) {
            messages = getRealmByUser(realmUser).getRealmChatService().getOlderChatMessagesForChat(realmChat.getRealmEntityId(), realmUser.getRealmEntityId(), offset, context);
            syncChatMessagesForChat(realmChat, context, messages);
        } else {
            messages = java.util.Collections.emptyList();
            Log.e(this.getClass().getSimpleName(), "Not chat found - chat id: " + realmChat.getEntityId());
        }

        return java.util.Collections.unmodifiableList(messages);
    }

    @Override
    public void syncChat(@Nonnull RealmEntity realmChat, @Nonnull RealmEntity realmUser) {
        // todo serso: check if OK
        syncNewerChatMessagesForChat(realmChat, realmUser);
    }

    @Nullable
    @Override
    public RealmEntity getSecondUser(@Nonnull Chat chat) {
        boolean first = true;

        // todo serso: continue
/*        for (String userId : Splitter.on('_').split(chat.getId())) {
            if ( first ) {
                first = false;
            } else {
                return userId;
            }
        }*/

        return null;
    }

    @Override
    public void setChatIcon(@Nonnull ImageView imageView, @Nonnull Chat chat, @Nonnull User user) {
        final Drawable defaultChatIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final List<User> otherParticipants = this.getParticipantsExcept(chat.getRealmChat(), user.getRealmUser());

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
    public RealmEntity createPrivateChatId(@Nonnull RealmEntity realmUser, @Nonnull RealmEntity secondRealmUser) {
        return getRealmByUser(realmUser).newRealmEntity(realmUser.getRealmEntityId() + "_" + secondRealmUser.getRealmEntityId());
    }

    @Nonnull
    @Override
    public ChatMessage sendChatMessage(@Nonnull RealmEntity user, @Nonnull Chat chat, @Nonnull ChatMessage chatMessage) {
        final String chatMessageId = getRealmByUser(user).getRealmChatService().sendChatMessage(chat, chatMessage, context);

        final LiteChatMessageImpl msgResult = LiteChatMessageImpl.newInstance(chatMessageId);

        msgResult.setAuthor(getUserService().getUserById(user));
        if (chat.isPrivate()) {
            final RealmEntity secondUser = chat.getSecondUser();
            msgResult.setRecipient(getUserService().getUserById(secondUser));
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

    @Nonnull
    private ChatMessageService getChatMessageService() {
        return this.chatMessageService;
    }

    @Nonnull
    @Override
    public List<User> getParticipants(@Nonnull RealmEntity realmChat) {
        List<User> result;

        synchronized (chatParticipantsCache) {
            result = chatParticipantsCache.get(realmChat);
            if (result == null) {
                result = getChatDao(context).loadChatParticipants(realmChat.getEntityId());
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
    public List<User> getParticipantsExcept(@Nonnull RealmEntity realmChat, @Nonnull final RealmEntity realmUser) {
        final List<User> participants = getParticipants(realmChat);
        return Lists.newArrayList(Iterables.filter(participants, new Predicate<User>() {
            @Override
            public boolean apply(@javax.annotation.Nullable User input) {
                return input != null && !input.getRealmUser().equals(realmUser);
            }
        }));
    }

    @Nullable
    @Override
    public ChatMessage getLastMessage(@Nonnull RealmEntity realmChat) {
        ChatMessage result;

        synchronized (lastMessagesCache) {
            result = lastMessagesCache.get(realmChat);
            if (result == null) {
                result = getChatMessageDao(context).loadLastChatMessage(realmChat.getEntityId());
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
    private ChatDao getChatDao(@Nonnull Context context) {
        return RoboGuiceUtils.getInContextScope(context, chatDaoProvider);
    }

    @Override
    public void addChatEventListener(@Nonnull ChatEventListener chatEventListener) {
        this.listeners.addListener(chatEventListener);
    }

    @Override
    public void removeChatEventListener(@Nonnull ChatEventListener chatEventListener) {
        this.listeners.removeListener(chatEventListener);
    }

    @Override
    public void fireChatEvent(@Nonnull Chat chat, @Nonnull ChatEventType chatEventType, @Nullable Object data) {
        fireChatEvents(Arrays.asList(new ChatEvent(chat, chatEventType, data)));
    }

    @Override
    public void fireChatEvents(@Nonnull List<ChatEvent> chatEvents) {
        final Collection<ChatEventListener> listeners = this.listeners.getListeners();
        for (ChatEvent chatEvent : chatEvents) {
            Log.d(EVENT_TAG, "Event: " + chatEvent.getChatEventType() + " for chat: " + chatEvent.getChat().getRealmChat().getEntityId() + " with data: " + chatEvent.getData());
            for (ChatEventListener listener : listeners) {
                listener.onChatEvent(chatEvent.getChat(), chatEvent.getChatEventType(), chatEvent.getData());
            }
        }
    }

    @Override
    public void onChatEvent(@Nonnull Chat eventChat, @Nonnull ChatEventType chatEventType, @Nullable Object data) {
        synchronized (chatParticipantsCache) {

            if (chatEventType == ChatEventType.participant_added) {
                // participant added => need to add to list of cached participants
                if (data instanceof User) {
                    final User participant = ((User) data);
                    final List<User> participants = chatParticipantsCache.get(eventChat.getRealmChat());
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
                    final List<User> participants = chatParticipantsCache.get(eventChat.getRealmChat());
                    if (participants != null) {
                        participants.remove(participant);
                    }
                }
            }
        }

        synchronized (chatsById) {
            if ( chatEventType == ChatEventType.changed || chatEventType == ChatEventType.added || chatEventType == ChatEventType.last_message_changed ) {
                chatsById.put(eventChat.getRealmChat(), eventChat);
            }
        }


        final Map<Chat, ChatMessage> changesLastMessages = new HashMap<Chat, ChatMessage>();
        synchronized (lastMessagesCache) {

            if (chatEventType == ChatEventType.message_added) {
                if (data instanceof ChatMessage) {
                    final ChatMessage message = (ChatMessage) data;
                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getRealmChat());
                    if (messageFromCache == null || message.getSendDate().isAfter(messageFromCache.getSendDate()) ) {
                        lastMessagesCache.put(eventChat.getRealmChat(), message);
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

                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getRealmChat());
                    if (newestMessage != null && (messageFromCache == null || newestMessage.getSendDate().isAfter(messageFromCache.getSendDate()))) {
                        lastMessagesCache.put(eventChat.getRealmChat(), newestMessage);
                        changesLastMessages.put(eventChat, newestMessage);
                    }
                }
            }


            if (chatEventType == ChatEventType.message_changed) {
                if (data instanceof ChatMessage) {
                    final ChatMessage message = (ChatMessage) data;
                    final ChatMessage messageFromCache = lastMessagesCache.get(eventChat.getRealmChat());
                    if (messageFromCache == null || messageFromCache.equals(message)) {
                        lastMessagesCache.put(eventChat.getRealmChat(), message);
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
    public void onUserEvent(@Nonnull User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
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
