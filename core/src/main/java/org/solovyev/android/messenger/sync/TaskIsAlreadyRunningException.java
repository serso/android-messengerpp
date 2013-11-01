package org.solovyev.android.messenger.sync;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.core.R;

import static org.solovyev.android.messenger.App.showToast;

public class TaskIsAlreadyRunningException extends Exception {

	@Nonnull
	private SyncTask syncTask;

	public TaskIsAlreadyRunningException(@Nonnull SyncTask syncTask) {
		this.syncTask = syncTask;
	}

	@Nonnull
	public SyncTask getSyncTask() {
		return syncTask;
	}

	public void showMessage() {
		showToast(R.string.mpp_task_is_already_running);
	}
}
