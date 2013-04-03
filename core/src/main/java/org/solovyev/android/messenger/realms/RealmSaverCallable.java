package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:17 PM
 */
final class RealmSaverCallable implements Callable<Realm> {

    @Nonnull
    private final RealmBuilder realmBuilder;

    public RealmSaverCallable(@Nonnull RealmBuilder realmBuilder) {
        this.realmBuilder = realmBuilder;
    }

    @Override
    public Realm call() throws InvalidCredentialsException, RealmAlreadyExistsException {
        return MessengerApplication.getServiceLocator().getRealmService().saveRealm(realmBuilder);
    }
}
