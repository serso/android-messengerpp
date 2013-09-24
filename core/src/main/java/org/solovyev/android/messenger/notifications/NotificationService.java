package org.solovyev.android.messenger.notifications;

import java.util.List;

import javax.annotation.Nonnull;

public interface NotificationService {

	void add(@Nonnull Notification notification);

	@Nonnull
	List<Notification> getNotifications();

	boolean existNotifications();

	void remove(@Nonnull Notification notification);

	void remove(int notificationId);
}
