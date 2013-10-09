package org.solovyev.android.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static org.solovyev.android.messenger.App.startBackgroundService;

public class OnBootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		startBackgroundService();
	}
}
