package org.solovyev.android.messenger.notifications;

import android.app.Application;
import android.content.Context;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerEventType;
import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.android.messenger.MessengerNotification;
import org.solovyev.common.msg.Message;
import org.solovyev.common.msg.MessageLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .build();

    @GuardedBy("notifications")
    @Nonnull
    private final List<Message> notifications = new ArrayList<Message>();

    @Nonnull
    private final Context context;

    @Inject
    public DefaultNotificationService(@Nonnull Application context) {
        this.context = context;
    }

    private void notify(@Nonnull final Message notification) {

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
                // notification is not in cache => check if it is already shown
                if (!notifications.contains(notification)) {
                    // not shown yet => add to cache and to shown notifications
                    recentNotifications.put(notification, notification);
                    notifications.add(notification);
                    notifyUser = true;
                }
            }
        }

        if (notifyUser) {
            messengerListeners.fireEvent(MessengerEventType.notification_added.newEvent(notification));
        }
    }

    @Override
    public void addNotification(int messageResId, @Nonnull MessageLevel level, @Nullable Object... parameters) {
        notify(new MessengerNotification(context, messageResId, level, parameters));
    }

    @Override
    public void addNotification(int messageResId, @Nonnull MessageLevel level, @Nonnull List<Object> parameters) {
        notify(new MessengerNotification(context, messageResId, level, parameters));
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

    @Override
    public void removeNotification(@Nonnull Message notification) {
        boolean removed;

        synchronized (notifications) {
            removed = notifications.remove(notification);
        }

        if (removed) {
            messengerListeners.fireEvent(MessengerEventType.notification_removed.newEvent(notification));
        }
    }
}
