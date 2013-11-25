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

package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.connection.BaseAccountConnection;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.newTag;

public abstract class LongPollAccountConnection extends BaseAccountConnection<Account> {

	public static final String TAG = newTag("LongPolling");

	@Nonnull
	private final RealmLongPollService realmLongPollService;

	protected LongPollAccountConnection(@Nonnull Account account,
										@Nonnull Context context,
										@Nonnull RealmLongPollService realmLongPollService,
										int retryCount) {
		super(account, context, true, retryCount);
		this.realmLongPollService = realmLongPollService;
	}

	@Override
	public void start0() throws AccountConnectionException {
		// first loop guarantees that if something gone wrong we will initiate new long polling session
		while (!isStopped()) {
			try {

				Log.i(TAG, "Long polling initiated!");
				Object longPollingData = realmLongPollService.startLongPolling();

				// second loop do long poll job for one session
				while (!isStopped()) {
					Log.i(TAG, "Long polling started!");

					final LongPollResult longPollResult = realmLongPollService.waitForResult(longPollingData);
					if (longPollResult != null) {
						longPollingData = longPollResult.updateLongPollServerData(longPollingData);
						longPollResult.doUpdates(getAccount());
					}

					Log.i(TAG, "Long polling ended!");

				}

			} catch (RuntimeException e) {
				throw new AccountConnectionException(getAccount().getId(), e);
			} catch (AccountException e) {
				throw new AccountConnectionException(getAccount().getId(), e);
			}
		}
	}

	@Override
	protected void stop0() {
	}

}
