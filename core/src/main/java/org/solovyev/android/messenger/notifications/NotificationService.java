package org.solovyev.android.messenger.notifications;

import org.solovyev.android.messenger.MessengerNotification;
import org.solovyev.common.msg.MessageLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface NotificationService {

	void addNotification(int messageResId, @Nonnull MessageLevel level, @Nullable Object... parameters);

	void addNotification(int messageResId, @Nonnull MessageLevel level, @Nonnull List<Object> parameters);

	void addNotification(int messageResId, @Nonnull MessageLevel level, @Nonnull Runnable oneClickSolution, @Nullable Object... parameters);

	void addNotification(int messageResId, @Nonnull MessageLevel level, @Nonnull Runnable oneClickSolution, @Nonnull List<Object> parameters);

	@Nonnull
	List<MessengerNotification> getNotifications();

	boolean existNotifications();

	void removeNotification(@Nonnull MessengerNotification notification);

	void removeNotification(int notificationId);
}
