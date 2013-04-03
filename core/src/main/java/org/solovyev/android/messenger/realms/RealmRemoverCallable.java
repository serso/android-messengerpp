package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:32 PM
 */
final class RealmRemoverCallable implements Callable<Realm> {

    @Nonnull
    private final Realm realm;

    RealmRemoverCallable(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Override
    public Realm call() {
        MessengerApplication.getServiceLocator().getRealmService().removeRealm(realm.getId());

        return realm;
    }
}
