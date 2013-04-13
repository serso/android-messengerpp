package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.widget.Toast;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:05 PM
 */
class RealmSaverCallable implements Callable<Realm> {

    @Nonnull
    static final String TASK_NAME = "realm-save";

    @Nonnull
    private final RealmBuilder realmBuilder;

    RealmSaverCallable(@Nonnull RealmBuilder realmBuilder) {
        this.realmBuilder = realmBuilder;
    }

    @Override
    public Realm call() throws InvalidCredentialsException, RealmAlreadyExistsException {
        return MessengerApplication.getServiceLocator().getRealmService().saveRealm(realmBuilder);
    }
}