package org.solovyev.android.messenger.sync;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmService;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.TimerTask;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 11:17 PM
 */
public class SyncTimerTask extends TimerTask {

	@Nonnull
	private final WeakReference<Context> contextRef;

	public SyncTimerTask(@Nonnull Context context) {
		this.contextRef = new WeakReference<Context>(context);
	}

	@Override
	public void run() {
		final Context context = this.contextRef.get();
		if (context != null) {
			for (RealmDef realm : RoboGuice.getInjector(context).getInstance(RealmService.class).getRealmDefs()) {
				final SyncData syncData = new SyncDataImpl(realm.getId());

				for (SyncTask syncTask : SyncTask.values()) {
					if (syncTask.isTime(syncData)) {
						Log.i("SyncTask", "Sync task started: " + syncTask);
						syncTask.doTask(syncData);
						Log.i("SyncTask", "Sync task ended: " + syncTask);
					}
				}

			}
		}
	}
}
