package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.inject.Inject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.MessengerRealmsActivity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.User;
import roboguice.activity.RoboActivity;

import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:47 PM
 */
public class MessengerStartActivity extends RoboActivity {

    @Inject
    @Nonnull
    private AuthService authService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private SyncService syncService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Collection<Realm> realms = realmService.getRealms();
        if (realms.isEmpty()) {

            boolean atLeastOneRealmLogged = false;
            boolean syncDone = false;

            for (Realm realm : realms) {
                try {
                    final User user = authService.getUser(realm.getRealmDef().getId());

                    if (!user.getUserSyncData().isFirstSyncDone()) {
                        if (!syncDone) {
                            syncDone = true;

                            // user is logged first time => sync all data
                            try {
                                syncService.syncAll(this);
                            } catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
                                // do not care
                            }
                        }
                    } else {
                        // prefetch data
                        new PreloadCachedData(this).execute(user);
                    }

                    atLeastOneRealmLogged = true;
                } catch (UserIsNotLoggedInException e) {
                    Log.e(MessengerStartActivity.class.getSimpleName(), e.getMessage(), e);
                }
            }


            if (atLeastOneRealmLogged) {
                MessengerMainActivity.startActivity(this);
            } else {
                MessengerRealmsActivity.startActivity(this);
            }
        } else {
            MessengerRealmsActivity.startActivity(this);
        }

        this.finish();
    }

    private static final class PreloadCachedData extends MessengerAsyncTask<User, Void, Void> {

        private PreloadCachedData(@Nonnull Context context) {
            super(context);
        }

        @Override
        protected Void doWork(@Nonnull List<User> users) {
            Context context = getContext();
            if (context != null) {
                for (User user : users) {
                    MessengerApplication.getServiceLocator().getUserService().getUserContacts(user.getRealmUser());
                    MessengerApplication.getServiceLocator().getUserService().getUserChats(user.getRealmUser());
                }
            }

            return null;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {

        }
    }
}
