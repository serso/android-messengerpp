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

import java.lang.ref.WeakReference;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;

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
