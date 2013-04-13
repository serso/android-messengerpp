package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:12 PM
 */
final class RealmChangeStateCallable implements Callable<Realm> {

    @Nonnull
    static final String TASK_NAME = "realm-change-state";

    @Nonnull
    private final Realm realm;

    RealmChangeStateCallable(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Override
    public Realm call() throws Exception {
        final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();
        return realmService.changeRealmState(realm, realm.isEnabled() ? RealmState.disabled_by_user : RealmState.enabled);
    }
}
