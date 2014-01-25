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

package org.solovyev.android.messenger.accounts.connection;

import android.util.Log;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_app;
import static org.solovyev.android.messenger.accounts.connection.DefaultAccountConnections.TAG;

class ConnectionRunnable implements Runnable {

	public static final long DEFAULT_RECOVERY_SLEEP_MILLIS = 3000L;

	@Nonnull
	private final AccountConnection connection;

	private final long recoverySleepMillis;

	public ConnectionRunnable(@Nonnull AccountConnection connection) {
		this(connection, DEFAULT_RECOVERY_SLEEP_MILLIS);
	}

	ConnectionRunnable(@Nonnull AccountConnection connection, long recoverySleepMillis) {
		this.connection = connection;
		this.recoverySleepMillis = recoverySleepMillis;
	}

	@Override
	public void run() {
		final int retryCount = connection.getRetryCount();
		int attempt = 0;

		AccountConnectionException lastError = startConnection(attempt++);
		while (lastError != null) {
			if (attempt > retryCount) {
				onMaxAttemptsReached(lastError);
				break;
			} else {
				lastError = startConnectionDelayed(attempt++);
				if (connection.isStopped()) {
					break;
				}
			}
		}
	}

	private AccountConnectionException startConnection(int attempt) {
		Log.d(TAG, "Account start requested, attempt: " + attempt);

		try {
			if (connection.getAccount().isEnabled()) {
				Log.d(TAG, "Account is enabled => starting connection...");
				connection.start();
				Log.d(TAG, "Connection is successfully established => no more work is needed on background thread. Terminating...");
			}
		} catch (AccountConnectionException e) {
			return onConnectionException(attempt, e);
		} catch (Throwable e) {
			return onConnectionException(attempt, e);
		}

		return null;
	}

	private AccountConnectionException onConnectionException(int attempt, @Nonnull Throwable e) {
		return onConnectionException(attempt, new AccountConnectionException(connection.getAccount().getId(), e));
	}

	private AccountConnectionException onConnectionException(int attempt, AccountConnectionException e) {
		Log.w(TAG, "Account connection error occurred, connection attempt: " + attempt, e);
		return e;
	}

	private void onMaxAttemptsReached(@Nullable AccountConnectionException lastError) {
		Log.d(TAG, "Max retry count reached => stopping...");

		if (!connection.isStopped()) {
			connection.stop();

			if (lastError != null) {
				getExceptionHandler().handleException(lastError);
			}

			getAccountService().changeAccountState(connection.getAccount(), disabled_by_app);
		}
	}

	private AccountConnectionException startConnectionDelayed(int attempt) {
		AccountConnectionException exception = null;

		try {
			// let's wait a little bit - may be the exception was caused by connectivity problem
			Thread.sleep(recoverySleepMillis);
		} catch (InterruptedException e) {
		} finally {
			if (!connection.isStopped()) {
				exception = startConnection(attempt + 1);
			}
		}

		return exception;
	}
}
