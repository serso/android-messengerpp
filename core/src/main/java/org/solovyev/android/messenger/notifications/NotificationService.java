package org.solovyev.android.messenger.notifications;

import javax.annotation.Nonnull;
import java.util.List;

public interface NotificationService {

	void add(@Nonnull Notification notification);

	@Nonnull
	List<Notification> getNotifications();

	boolean existNotifications();

	void remove(@Nonnull Notification notification);

	void remove(int notificationId);
}
