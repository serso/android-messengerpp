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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import org.solovyev.android.messenger.core.R;
import roboguice.service.RoboService;

import javax.annotation.Nonnull;

public final class OngoingNotificationService extends RoboService {

	private static final int NOTIFICATION_ID_APP_IS_RUNNING = 10002029;

	public OngoingNotificationService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
		nb.setOngoing(true);
		nb.setSmallIcon(R.drawable.mpp_sb_icon);
		nb.setContentTitle(getString(R.string.mpp_notification_title));
		nb.setContentText(getString(R.string.mpp_notification_text));
		nb.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, StartActivity.class), 0));
		startForeground(NOTIFICATION_ID_APP_IS_RUNNING, nb.getNotification());
	}

	static void startOngoingNotificationService(@Nonnull Context context) {
		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(context, OngoingNotificationService.class);
		context.startService(serviceIntent);
	}

	static void stopOngoingNotificationService(@Nonnull Context context) {
		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(context, OngoingNotificationService.class);
		context.stopService(serviceIntent);
	}
}
