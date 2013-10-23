package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.connection.AbstractAccountConnection;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.newTag;

public abstract class LongPollAccountConnection extends AbstractAccountConnection<Account> {

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

					final User user = getAccount().getUser();
					final LongPollResult longPollResult = realmLongPollService.waitForResult(longPollingData);
					if (longPollResult != null) {
						longPollingData = longPollResult.updateLongPollServerData(longPollingData);
						longPollResult.doUpdates(user, getAccount());
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
