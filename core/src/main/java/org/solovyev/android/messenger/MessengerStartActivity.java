package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import com.google.inject.Inject;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.User;
import roboguice.activity.RoboActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        // todo serso: maybe move to Application or Service?
        // prefetch data and do synchronization

        boolean syncDone = true;

        for (Realm realm : realms) {
            final User user = realm.getUser();

            if (!user.getUserSyncData().isFirstSyncDone()) {
                syncDone = false;
            } else {
                // prefetch data
                new PreloadCachedData(this).execute(user);
            }
        }

        if (!syncDone) {
            // todo serso: actually synchronization must be done only for not synced realms (NOT for all as it is now)
            // user is logged first time => sync all data
            try {
                syncService.syncAll(syncDone);
            } catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
                // do not care
            }
        }

        MessengerMainActivity.startActivity(this);
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
                    MessengerApplication.getServiceLocator().getUserService().getUserContacts(user.getEntity());
                    MessengerApplication.getServiceLocator().getUserService().getUserChats(user.getEntity());
                }
            }

            return null;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {

        }
    }
}
