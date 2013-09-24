package org.solovyev.android.messenger;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import roboguice.service.RoboService;

import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
public final class OngoingNotificationService extends RoboService {

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

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
		nb.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MessengerStartActivity.class), 0));
		startForeground(NOTIFICATION_ID_APP_IS_RUNNING, nb.getNotification());
	}
}
