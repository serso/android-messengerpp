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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import static org.solovyev.android.messenger.App.getAccountService;

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
		for (Account account : getAccountService().getEnabledAccounts()) {
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
