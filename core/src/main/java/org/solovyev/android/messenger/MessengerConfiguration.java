package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration extends MessengerApiProvider {

    @NotNull
    Realm getRealm();

    @NotNull
    DaoLocator getDaoLocator();

    @NotNull
    ServiceLocator getServiceLocator();
}
