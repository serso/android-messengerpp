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

package org.solovyev.android.messenger.sync;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DefaultSyncService implements SyncService {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

    /*
	**********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final Set<SyncTask> runningTasks = EnumSet.noneOf(SyncTask.class);

	@GuardedBy("syncAllTaskRunning")
	@Nonnull
	private final AtomicBoolean syncAllTaskRunning = new AtomicBoolean(false);

	@Nonnull
	private final Executor executor = Executors.newSingleThreadExecutor();

	@Nonnull
	private final JEventListener<AccountEvent> realmEventListener = new RealmEventListener();

	@Override
	public void init() {
		accountService.addListener(realmEventListener);
	}

	@Override
	public void syncAll(final boolean force) throws SyncAllTaskIsAlreadyRunning {
		startSyncAllTask(accountService.getEnabledAccounts(), force);
	}

	@Override
	public boolean isSyncAllTaskRunning() {
		synchronized (syncAllTaskRunning) {
			return syncAllTaskRunning.get();
		}
	}

	/**
	 * Method checks if 'all synchronization task' is not running and starts one with specified parameters
	 *
	 * @param accounts realms for which synchronization should be done
	 * @param force    force synchronization. See {@link SyncService#syncAll(boolean)}
	 * @throws SyncAllTaskIsAlreadyRunning thrown when task if 'all synchronization task' is already running
	 */
	private void startSyncAllTask(@Nonnull Collection<Account> accounts, boolean force) throws SyncAllTaskIsAlreadyRunning {
		synchronized (syncAllTaskRunning) {
			if (syncAllTaskRunning.get()) {
				throw new SyncAllTaskIsAlreadyRunning();
			} else {
				syncAllTaskRunning.set(true);
			}
		}

		executor.execute(new SyncRunnable(force, accounts));
	}

	@Override
	public void syncAllForAccount(@Nonnull Account account, boolean force) throws SyncAllTaskIsAlreadyRunning {
		startSyncAllTask(Arrays.asList(account), force);
	}

	@Override
	public void sync(@Nonnull SyncTask syncTask, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException {
		checkRunningTask(syncTask);

		new ServiceSyncAsyncTask(syncTask, afterSyncCallback).executeInParallel();
	}

	private void checkRunningTask(SyncTask syncTask) throws TaskIsAlreadyRunningException {
		synchronized (runningTasks) {
			if (runningTasks.contains(syncTask)) {
				throw new TaskIsAlreadyRunningException(syncTask);
			}
			runningTasks.add(syncTask);
		}
	}

	@Override
	public void waitWhileSyncFinished() {
		while (this.isSyncAllTaskRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

    /*
	**********************************************************************
    *
    *                           INNER CLASSES
    *
    **********************************************************************
    */

	private class ServiceSyncAsyncTask extends SyncAsyncTask {

		@Nonnull
		private final SyncTask syncTask;

		@Nullable
		private final Runnable afterSyncCallback;

		public ServiceSyncAsyncTask(@Nonnull SyncTask syncTask, @Nullable Runnable afterSyncCallback) {
			super(Arrays.asList(syncTask));
			this.syncTask = syncTask;
			this.afterSyncCallback = afterSyncCallback;
		}

		@Override
		protected void onSuccessPostExecute(@Nullable Void result) {
			releaseRunningTask(syncTask);
			super.onSuccessPostExecute(result);

			if (afterSyncCallback != null) {
				afterSyncCallback.run();
			}
		}

		@Override
		protected void onCancelled() {
			// just in case
			releaseRunningTask(syncTask);
			super.onCancelled();

			if (afterSyncCallback != null) {
				afterSyncCallback.run();
			}
		}

		@Override
		protected void onFailurePostExecute(@Nonnull Exception e) {
			releaseRunningTask(syncTask);
			super.onFailurePostExecute(e);

			if (afterSyncCallback != null) {
				afterSyncCallback.run();
			}
		}

		@Override
		public String toString() {
			return "ServiceSyncAsyncTask{" +
					"syncTask=" + syncTask +
					'}';
		}
	}

	private void releaseRunningTask(@Nonnull SyncTask syncTask) {
		synchronized (runningTasks) {
			runningTasks.remove(syncTask);
		}
	}

	private class SyncRunnable implements Runnable {

		private final boolean force;

		@Nonnull
		private final Collection<Account> accounts;

		public SyncRunnable(boolean force, @Nonnull Collection<Account> accounts) {
			this.force = force;
			this.accounts = accounts;
		}

		@Override
		public void run() {
			try {

				for (Account account : accounts) {
					final SyncData syncData = new SyncDataImpl(account.getId());

					for (SyncTask syncTask : SyncTask.values()) {
						try {
							try {
								checkRunningTask(syncTask);
								if (force || syncTask.isTime(syncData)) {
									syncTask.doTask(syncData);
								}
							} finally {
								releaseRunningTask(syncTask);
							}
						} catch (TaskIsAlreadyRunningException e) {
							// ok, task is already running => start another task
						} catch (RuntimeException e) {
							App.getExceptionHandler().handleException(e);
						}
					}
				}

			} finally {
				synchronized (syncAllTaskRunning) {
					syncAllTaskRunning.set(false);
				}
			}
		}
	}

	private final class RealmEventListener extends AbstractJEventListener<AccountEvent> {

		private RealmEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			switch (event.getType()) {
				case created:
				case changed:
					try {
						syncAllForAccount(event.getAccount(), true);
					} catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
						// ok, do not care
					}
					break;
			}
		}
	}
}
