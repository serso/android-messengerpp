package org.solovyev.android.messenger.sync;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:58 PM
 */
class SyncAsyncTask extends MessengerAsyncTask<Void, Void, Void> {

	@Nonnull
	private final List<SyncTask> syncTasks;

	public SyncAsyncTask(@Nonnull List<SyncTask> syncTasks) {
		super();
		this.syncTasks = syncTasks;
	}


	@Override
	protected Void doWork(@Nonnull List<Void> voids) {
		for (Account account : App.getAccountService().getEnabledAccounts()) {
			final SyncData syncData = new SyncDataImpl(account.getId());

			for (SyncTask syncTask : syncTasks) {
				syncTask.doTask(syncData);
			}
		}

		return null;
	}

	@Override
	protected void onSuccessPostExecute(@Nullable Void result) {
	}
}
