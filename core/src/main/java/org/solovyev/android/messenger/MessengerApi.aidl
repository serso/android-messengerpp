package org.solovyev.android.messenger;

import org.solovyev.android.messenger.ServiceCallback;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:38 PM
 */
interface MessengerApi {

    void loginUser(String realm, String login, String password, ServiceCallback callback);
}
