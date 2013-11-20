package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;

public class SyncRefreshListener extends AbstractOnRefreshListener {

	@Nonnull
	private final SyncTask task;

	public SyncRefreshListener(@Nonnull SyncTask task) {
		this.task = task;
	}

	@Override
	public void onRefresh() {
		try {
			App.getSyncService().sync(task, new Runnable() {
				@Override
				public void run() {
					completeRefresh();
				}
			});
		} catch (TaskIsAlreadyRunningException e) {
			completeRefresh();
		}
	}
}
