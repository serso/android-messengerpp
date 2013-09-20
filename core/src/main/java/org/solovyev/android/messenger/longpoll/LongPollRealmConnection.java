package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:53 PM
 */
public abstract class LongPollRealmConnection extends AbstractRealmConnection<Account> {

	public static final String TAG = "LongPolling";
	@Nonnull
	private final RealmLongPollService realmLongPollService;

	protected LongPollRealmConnection(@Nonnull Account account,
									  @Nonnull Context context,
									  @Nonnull RealmLongPollService realmLongPollService) {
		super(account, context);
		this.realmLongPollService = realmLongPollService;
	}

	@Override
	public void doWork() throws AccountConnectionException {
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
	protected void stopWork() {
	}

}
