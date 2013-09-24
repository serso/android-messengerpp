package org.solovyev.android.messenger.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.MessengerEventType;
import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.common.msg.Message;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
	private final List<Notification> notifications = new ArrayList<Notification>();

	public DefaultNotificationService() {
	}

	private void notify(@Nonnull final Notification notification) {

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
	public void add(@Nonnull Notification notification) {
		notify(notification);
	}

	@Override
	@Nonnull
	public List<Notification> getNotifications() {
		synchronized (notifications) {
			return new ArrayList<Notification>(notifications);
		}
	}

	@Override
	public boolean existNotifications() {
		synchronized (notifications) {
			return !notifications.isEmpty();
		}
	}

	@Override
	public void remove(@Nonnull Notification notification) {
		boolean removed;

		synchronized (notifications) {
			removed = notifications.remove(notification);
		}

		if (removed) {
			messengerListeners.fireEvent(MessengerEventType.notification_removed.newEvent(notification));
		}
	}

	@Override
	public void remove(int notificationId) {
		final List<Message> removedNotifications = new ArrayList<Message>();

		final String messageCode = String.valueOf(notificationId);
		synchronized (notifications) {
			Iterables.removeIf(notifications, PredicateSpy.spyOn(new Predicate<Message>() {
				@Override
				public boolean apply(@Nullable Message notification) {
					return notification != null && notification.getMessageCode().equals(messageCode);
				}
			}, removedNotifications));
		}

		if (!removedNotifications.isEmpty()) {
			for (Message removedNotification : removedNotifications) {
				messengerListeners.fireEvent(MessengerEventType.notification_removed.newEvent(removedNotification));
			}
		}
	}
}
