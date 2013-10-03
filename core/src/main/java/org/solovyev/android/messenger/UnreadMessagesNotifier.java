package org.solovyev.android.messenger;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.core.R;
import org.solovyev.common.listeners.AbstractJEventListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 3/25/13
 * Time: 12:09 AM
 */
@Singleton
public final class UnreadMessagesNotifier extends AbstractJEventListener<MessengerEvent> {

	private static final int NOTIFICATION_ID_UNREAD_MESSAGES = 10002030;

	@Nonnull
	private final Application context;

	@Inject
	public UnreadMessagesNotifier(@Nonnull Application context, @Nonnull MessengerListeners messengerListeners) {
		super(MessengerEvent.class);
		this.context = context;

		messengerListeners.addListener(this);
	}

	@Override
	public void onEvent(@Nonnull MessengerEvent event) {
		switch (event.getType()) {
			case unread_messages_count_changed:
				final Integer unreadMessagesCount = event.getDataAsInteger();

				if (unreadMessagesCount == 0) {
					final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					nm.cancel(NOTIFICATION_ID_UNREAD_MESSAGES);
				} else {
					final ActivityManager.RunningTaskInfo foregroundTask = getForegroundTask();
					if (foregroundTask == null || !foregroundTask.topActivity.getPackageName().equals(context.getPackageName())) {
						// todo serso: make proper notification (unread messages text, small icon, etc)
						// we are not at the top => show notification
						final NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
						nb.setSmallIcon(R.drawable.mpp_sb_unread_messages_counter);
						nb.setContentText(context.getResources().getQuantityString(R.plurals.mpp_unread_messages_count_notification, unreadMessagesCount, unreadMessagesCount));
						nb.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, StartActivity.class), 0));
						nb.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
						final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						nm.notify(NOTIFICATION_ID_UNREAD_MESSAGES, nb.getNotification());
					}
				}
				break;
		}
	}

	@Nullable
	private ActivityManager.RunningTaskInfo getForegroundTask() {
		final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// The first in the list of RunningTasks is always the foreground task.
		final List<ActivityManager.RunningTaskInfo> foregroundTasks = am.getRunningTasks(1);
		if (foregroundTasks != null && !foregroundTasks.isEmpty()) {
			return foregroundTasks.get(0);
		} else {
			return null;
		}
	}
}
