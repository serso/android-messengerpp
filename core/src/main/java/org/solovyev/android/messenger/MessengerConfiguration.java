package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.security.ApiAuthenticator;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration extends MessengerApiProvider {

    @NotNull
    String getRealm();

    @NotNull
    ApiAuthenticator getAuthenticator();

    @NotNull
    DaoLocator getDaoLocator();

    @NotNull
    ServiceLocator getServiceLocator();
}
