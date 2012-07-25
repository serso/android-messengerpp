package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:53 PM
 */
public class LongPollRealmConnection extends AbstractRealmConnection {

    @NotNull
    private final RealmLongPollService realmLongPollService;

    public LongPollRealmConnection(@NotNull Realm realm,
                                   @NotNull Context context,
                                   @NotNull RealmLongPollService realmLongPollService) {
        super(realm, context);
        this.realmLongPollService = realmLongPollService;
    }

    @Override
    public void doWork() throws ContextIsNotActiveException {
        // first loop guarantees that if something gone wrong we will initiate new long polling session
        while (!isStopped()) {
            try {

                Log.i("LongPolling", "Long polling initiated!");
                Object longPollingData = realmLongPollService.startLongPolling();

                // second loop do long poll job for one session
                while (!isStopped()) {
                    Log.i("LongPolling", "Long polling started!");

                    final User user = getServiceLocator().getAuthService().getUser(getRealm().getId(), getContext());
                    final LongPollResult longPollResult = realmLongPollService.waitForResult(longPollingData);
                    if (longPollResult != null) {
                        longPollingData = longPollResult.updateLongPollServerData(longPollingData);
                        longPollResult.doUpdates(user, getContext());
                    }

                    Log.i("LongPolling", "Long polling ended!");

                }

            } catch (RuntimeException e) {
                Log.e("LongPolling", e.getMessage(), e);
            } catch (UserIsNotLoggedInException e) {
                // wait for login
            }
        }
    }
}
