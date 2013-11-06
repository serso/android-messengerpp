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

import android.content.Context;
import android.widget.Toast;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;

import static org.solovyev.android.messenger.App.showToast;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 5:42 PM
 */
public class SyncAllAsyncTask extends MessengerAsyncTask<Void, Void, Void> {

	@Nullable
	private final Account account;

	@Nonnull
	private final SyncService syncService;

	private SyncAllAsyncTask(@Nonnull Context context, @Nonnull SyncService syncService, @Nullable Account account) {
		super(context);
		this.account = account;
		this.syncService = syncService;
	}

	@Nonnull
	public static SyncAllAsyncTask newForAllAccounts(@Nonnull Context context, @Nonnull SyncService syncService) {
		return new SyncAllAsyncTask(context, syncService, null);
	}

	@Nonnull
	public static SyncAllAsyncTask newForAccount(@Nonnull Context context, @Nonnull SyncService syncService, @Nonnull Account account) {
		return new SyncAllAsyncTask(context, syncService, account);
	}

	@Override
	protected Void doWork(@Nonnull List<Void> voids) {
		Context context = getContext();
		if (context != null) {
			try {
				if (account == null) {
					syncService.syncAll(true);
				} else {
					syncService.syncAllForAccount(account, true);
				}
			} catch (SyncAllTaskIsAlreadyRunning e) {
				throwException(e);
			}
		}
		return null;
	}

	@Override
	protected void onSuccessPostExecute(@Nullable Void result) {
	}

	@Override
	protected void onFailurePostExecute(@Nonnull Exception e) {
		if (e instanceof SyncAllTaskIsAlreadyRunning) {
			final Context context = getContext();
			if (context != null) {
				showToast(R.string.mpp_sync_is_already_running);
			}
		} else {
			super.onFailurePostExecute(e);
		}
	}
}
