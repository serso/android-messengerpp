package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.users.MessengerFriendsActivity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:47 PM
 */
public class MessengerStartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AuthServiceFacade asf = MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthServiceFacade();
        if ( asf.isUserLoggedIn() ) {
            // user is logged => sync all data
            MessengerConfigurationImpl.getInstance().getServiceLocator().getSyncService().syncAll(this);

            MessengerFriendsActivity.startActivity(this);
        } else {
            MessengerLoginActivity.startActivity(this);
        }
        this.finish();
    }
}
