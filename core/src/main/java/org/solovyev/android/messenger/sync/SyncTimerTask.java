package org.solovyev.android.messenger.sync;

import android.content.Context;
import android.util.Log;
import roboguice.RoboGuice;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;

import static org.solovyev.android.messenger.App.getAccountService;

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
			for (Account account: getAccountService().getEnabledAccounts()) {
				final SyncData syncData = new SyncDataImpl(account.getId());

				for (SyncTask syncTask : SyncTask.values()) {
					if (syncTask.isTime(syncData)) {
						syncTask.doTask(syncData);
					}
				}
			}
		}
	}
}
