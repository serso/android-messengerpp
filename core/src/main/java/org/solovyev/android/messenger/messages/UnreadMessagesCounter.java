package org.solovyev.android.messenger.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerEventType;
import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 3/23/13
 * Time: 3:49 PM
 */

@Singleton
public final class UnreadMessagesCounter implements JEventListener<ChatEvent>{

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    private static final int DELAY_LONG = 2500;
    private static final int DELAY_SHORT = 500;

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
    private ChatMessageDao chatMessageDao;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private MessengerListeners messengerListeners;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    private final Object lock;

    @Nonnull
    private final AtomicInteger counter = new AtomicInteger(0);

    @GuardedBy("counter")
    @Nonnull
    private final Map<Entity, AtomicInteger> countersByChats = new HashMap<Entity, AtomicInteger>();

    @Nonnull
    private final AtomicInteger runnablesCounter = new AtomicInteger(0);

    @Nonnull
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public UnreadMessagesCounter(@Nonnull PersistenceLock lock) {
        this.lock = lock;
    }

    public void init() {
        synchronized (lock) {
            counter.set(chatMessageDao.getUnreadMessagesCount());
        }
        // todo serso: init countersByChats
        chatService.addListener(this);
    }

    @Nonnull
    @Override
    public Class<ChatEvent> getEventType() {
        return ChatEvent.class;
    }

    @Override
    public void onEvent(@Nonnull ChatEvent event) {
        switch (event.getType()) {
            case message_read:
                handleReadMessage(event.getChat());
                break;
            case message_added:
                handleNewMessages(event.getChat(), Arrays.asList(event.getDataAsChatMessage()));
                break;
            case message_added_batch:
                handleNewMessages(event.getChat(), event.getDataAsChatMessages());
                break;
        }
    }

    private void handleReadMessage(@Nonnull Chat chat) {
        changeCounter(chat, -1);
        // many messages can be read at once (e.g. chat was opened => wait a little bit for them, too)
        fireCounterChanged(false);
    }

    private void changeCounter(@Nonnull Chat chat, int increment) {
        synchronized (counter) {
            counter.addAndGet(increment);
            if ( counter.get() < 0 ) {
                counter.set(0);
            }

            AtomicInteger counterByChat = countersByChats.get(chat.getEntity());
            if (counterByChat == null) {
                counterByChat = new AtomicInteger();
                countersByChats.put(chat.getEntity(), counterByChat);
            }

            counterByChat.addAndGet(increment);
            if ( counterByChat.get() < 0 ) {
                counterByChat.set(0);
            }
        }
    }

    private void handleNewMessages(@Nonnull Chat chat, @Nonnull List<ChatMessage> messages) {
        int unread = 0;
        for (ChatMessage message : messages) {
            if (!message.isRead()) {
                unread++;
            }
        }

        if (unread > 0) {
            changeCounter(chat, unread);
            fireCounterChanged(true);
        }
    }

    private void fireCounterChanged(boolean longDelay) {
        final int runnableIndex = runnablesCounter.incrementAndGet();

        // a little delay for performance improvement
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if ( runnableIndex == runnablesCounter.get() ) {
                    // no new runnables scheduled => can continue
                    messengerListeners.fireEvent(MessengerEventType.unread_messages_count_changed.newEvent(counter.get()));
                }
            }
        }, longDelay ? DELAY_LONG : DELAY_SHORT, TimeUnit.MILLISECONDS);
    }

    public int getUnreadMessagesCount() {
        return counter.get();
    }

    @Nullable
    public Entity getUnreadChat() {
        synchronized (counter) {
            for (Map.Entry<Entity, AtomicInteger> entry : countersByChats.entrySet()) {
                final AtomicInteger counterByChat = entry.getValue();
                if (counterByChat != null && counterByChat.get() > 0) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }
}
