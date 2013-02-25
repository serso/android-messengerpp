package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:53 PM
 */
public class LongPollRealmConnection extends AbstractRealmConnection {

    public static final String TAG = "LongPolling";
    @NotNull
    private final RealmLongPollService realmLongPollService;

    public LongPollRealmConnection(@NotNull RealmDef realm,
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

                Log.i(TAG, "Long polling initiated!");
                Object longPollingData = realmLongPollService.startLongPolling();

                // second loop do long poll job for one session
                while (!isStopped()) {
                    Log.i(TAG, "Long polling started!");

                    final User user = AbstractMessengerApplication.getServiceLocator().getAuthService().getUser(getRealm().getId(), getContext());
                    final LongPollResult longPollResult = realmLongPollService.waitForResult(longPollingData);
                    if (longPollResult != null) {
                        longPollingData = longPollResult.updateLongPollServerData(longPollingData);
                        longPollResult.doUpdates(user, getContext());
                    }

                    Log.i(TAG, "Long polling ended!");

                }

            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (UserIsNotLoggedInException e) {
                waitForLogin();
            }
        }
    }

}
