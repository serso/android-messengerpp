package org.solovyev.android.messenger.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
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

    }

    @Nonnull
    @Override
    public Class<ChatEvent> getEventType() {
        return ChatEvent.class;
    }

    @Override
    public void onEvent(@Nonnull ChatEvent event) {
        switch (event.getType()) {
            case message_added:
                handleNewMessages(Arrays.asList(event.getDataAsChatMessage()));
                break;
            case message_added_batch:
                handleNewMessages(event.getDataAsChatMessages());
                break;
        }
    }

    private void handleNewMessages(@Nonnull List<ChatMessage> messages) {
        boolean counterChanged = false;

        for (ChatMessage message : messages) {
            if ( !message.isRead() ) {
                counter.incrementAndGet();
                counterChanged = true;
            }
        }

        if ( counterChanged ) {
            final int runnableIndex = runnablesCounter.incrementAndGet();
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if ( runnableIndex == runnablesCounter.get() ) {
                        // no new runnables scheduled => can continue
                    }
                }
            }, 5, TimeUnit.SECONDS);
        }
    }
}
