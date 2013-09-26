package org.solovyev.android.messenger.accounts.connection;

import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountState;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.accounts.connection.AccountConnections.TAG;

class ConnectionRunnable implements Runnable {

	private static final int RETRY_CONNECTION_ATTEMPT_COUNT = 5;

	@Nonnull
	private final AccountConnection connection;

	public ConnectionRunnable(@Nonnull AccountConnection connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		startConnection(0, null);
	}

	private void startConnection(int attempt, @Nullable AccountConnectionException lastError) {
		Log.d(TAG, "Account start requested, attempt: " + attempt);

		if (attempt > RETRY_CONNECTION_ATTEMPT_COUNT) {
			onMaxAttemptsReached(lastError);
		} else {
			if (connection.isStopped()) {
				try {
					if (connection.getAccount().isEnabled()) {
						Log.d(TAG, "Account is enabled => starting connection...");
						connection.start();
					}
				} catch (AccountConnectionException e) {
					Log.w(TAG, "Account connection error occurred, connection attempt: " + attempt, e);

					if (!connection.isStopped()) {
						connection.stop();
					}

					startConnectionDelayed(attempt, e);
				}
			}
		}
	}

	private void onMaxAttemptsReached(@Nullable AccountConnectionException lastError) {
		Log.d(TAG, "Max retry count reached => stopping...");

		if (!connection.isStopped()) {
			connection.stop();
		}

		if (lastError != null) {
			getExceptionHandler().handleException(lastError);
		}

		getAccountService().changeAccountState(connection.getAccount(), AccountState.disabled_by_app);
	}

	private void startConnectionDelayed(int attempt, @Nullable AccountConnectionException lastException) {
		try {
			// let's wait a little bit - may be the exception was caused by connectivity problem
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			startConnection(attempt + 1, lastException);
		}
	}
}
