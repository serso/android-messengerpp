package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.users.MessengerContactsActivity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:47 PM
 */
public class MessengerStartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AuthService as = MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthService();
        final String realm = MessengerConfigurationImpl.getInstance().getRealm().getId();
        if ( as.isUserLoggedIn(realm) ) {
            // user is logged => sync all data
            MessengerConfigurationImpl.getInstance().getServiceLocator().getSyncService().syncAll(this);

            MessengerContactsActivity.startActivity(this);
        } else {
            MessengerLoginActivity.startActivity(this);
        }
        this.finish();
    }
}
