package org.solovyev.android.messenger;

import android.os.Bundle;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.MessengerContactsActivity;
import roboguice.activity.RoboActivity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:47 PM
 */
public class MessengerStartActivity extends RoboActivity {

    @Inject
    @NotNull
    private AuthService authService;

    @Inject
    @NotNull
    private Realm realm;

    @Inject
    @NotNull
    private SyncService syncService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( authService.isUserLoggedIn(realm.getId()) ) {
            // user is logged => sync all data
            syncService.syncAll(this);

            MessengerContactsActivity.startActivity(this);
        } else {
            MessengerLoginActivity.startActivity(this);
        }
        this.finish();
    }
}
