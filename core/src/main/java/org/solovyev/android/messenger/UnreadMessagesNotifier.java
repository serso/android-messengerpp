/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.listeners.AbstractJEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static android.app.PendingIntent.getActivity;
import static org.solovyev.android.messenger.StartActivity.newUnreadMessagesStartIntent;

@Singleton
public final class UnreadMessagesNotifier extends AbstractJEventListener<MessengerEvent> {

	private static final int NOTIFICATION_ID_UNREAD_MESSAGES = 10002030;

	@Inject
	@Nonnull
	private MessengerListeners messengerListeners;

	@Nonnull
	private final Context context;

	@Inject
	public UnreadMessagesNotifier(@Nonnull Application context) {
		super(MessengerEvent.class);
		this.context = context;
	}

	public void init() {
		messengerListeners.addListener(this);
	}

	@Override
	public void onEvent(@Nonnull MessengerEvent event) {
		switch (event.getType()) {
			case unread_messages_count_changed:
				final Integer unreadMessagesCount = event.getDataAsInteger();

				final NotificationManager nm = getNotificationManager();

				// cancel last notification (if needed new notification will be created later)
				nm.cancel(NOTIFICATION_ID_UNREAD_MESSAGES);

				if (unreadMessagesCount > 0) {
					if (!isAppShown()) {
						// todo serso: make proper notification (unread messages text, small icon, etc)
						// we are not at the top => show notification
						final NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
						nb.setSmallIcon(R.drawable.mpp_sb_unread_messages_icon);
						nb.setContentTitle(context.getResources().getQuantityString(R.plurals.mpp_unread_messages_count_notification, unreadMessagesCount, unreadMessagesCount));
						nb.setContentText(context.getString(R.string.mpp_notification_text));
						nb.setContentIntent(getActivity(context, 0, newUnreadMessagesStartIntent(context), 0));
						nb.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
						nm.notify(NOTIFICATION_ID_UNREAD_MESSAGES, nb.getNotification());
					}
				}
				break;
		}
	}

	private boolean isAppShown() {
		final ActivityManager.RunningTaskInfo foregroundTask = getForegroundTask();
		return foregroundTask != null && foregroundTask.topActivity.getPackageName().equals(context.getPackageName());
	}

	@Nonnull
	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
