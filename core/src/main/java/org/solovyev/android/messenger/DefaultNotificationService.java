package org.solovyev.android.messenger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.common.msg.Message;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public final class DefaultNotificationService implements NotificationService {

    @Inject
    @Nonnull
    private MessengerListeners messengerListeners;

    @GuardedBy("notifications")
    @Nonnull
    private final Cache<Message, Message> recentNotifications = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    @GuardedBy("notifications")
    @Nonnull
    private final List<Message> notifications = new ArrayList<Message>();

    @Override
    public void notify(@Nonnull final Message notification) {

        boolean notifyUser = false;

        synchronized (notifications) {
            try {
                recentNotifications.get(notification, new Callable<Message>() {
                    @Override
                    public Message call() throws Exception {
                        throw new Exception();
                    }
                });

                // notification is in cache => do nothing as same notifications has already been shown recently
            } catch (ExecutionException e) {
                // notification is not in cache => put it there ans notify user
                recentNotifications.put(notification, notification);
                notifications.add(notification);
                notifyUser = true;
            }
        }

        if (notifyUser) {
            messengerListeners.fireEvent(MessengerEventType.notification_added.newEvent(notification));
        }
    }

    @Override
    @Nonnull
    public List<Message> getNotifications() {
        synchronized (notifications) {
            return new ArrayList<Message>(notifications);
        }
    }

    @Override
    public boolean existNotifications() {
        synchronized (notifications) {
            return !notifications.isEmpty();
        }
    }
}
