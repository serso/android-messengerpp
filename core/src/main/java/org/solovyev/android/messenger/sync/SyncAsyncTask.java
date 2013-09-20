package org.solovyev.android.messenger.sync;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.accounts.Account;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
		for (Account account : MessengerApplication.getServiceLocator().getAccountService().getEnabledAccounts()) {
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
