package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerContextTask;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:02 PM
 */
final class RealmRemoverTask extends MessengerContextTask<Context, Realm> {

    @Nonnull
    static final String TASK_NAME = "realm-remove";

    @Nonnull
    private final Realm realm;

    RealmRemoverTask(@Nonnull Realm realm) {
        super(TASK_NAME);
        this.realm = realm;
    }

    @Override
    public Realm call() {
        MessengerApplication.getServiceLocator().getRealmService().removeRealm(realm.getId());

        return realm;
    }

    @Override
    public void onSuccess(@Nonnull Context context, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.removed));
    }
}
