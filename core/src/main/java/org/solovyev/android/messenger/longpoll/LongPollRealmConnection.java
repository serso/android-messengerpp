package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmConnectionException;
import org.solovyev.android.messenger.realms.RealmException;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:53 PM
 */
public abstract class LongPollRealmConnection extends AbstractRealmConnection<Realm> {

	public static final String TAG = "LongPolling";
	@Nonnull
	private final RealmLongPollService realmLongPollService;

	protected LongPollRealmConnection(@Nonnull Realm realm,
									  @Nonnull Context context,
									  @Nonnull RealmLongPollService realmLongPollService) {
		super(realm, context);
		this.realmLongPollService = realmLongPollService;
	}

	@Override
	public void doWork() throws RealmConnectionException {
		// first loop guarantees that if something gone wrong we will initiate new long polling session
		while (!isStopped()) {
			try {

				Log.i(TAG, "Long polling initiated!");
				Object longPollingData = realmLongPollService.startLongPolling();

				// second loop do long poll job for one session
				while (!isStopped()) {
					Log.i(TAG, "Long polling started!");

					final User user = getRealm().getUser();
					final LongPollResult longPollResult = realmLongPollService.waitForResult(longPollingData);
					if (longPollResult != null) {
						longPollingData = longPollResult.updateLongPollServerData(longPollingData);
						longPollResult.doUpdates(user, getRealm());
					}

					Log.i(TAG, "Long polling ended!");

				}

			} catch (RuntimeException e) {
				throw new RealmConnectionException(e);
			} catch (RealmException e) {
				throw new RealmConnectionException(e);
			}
		}
	}

	@Override
	protected void stopWork() {
	}

}
