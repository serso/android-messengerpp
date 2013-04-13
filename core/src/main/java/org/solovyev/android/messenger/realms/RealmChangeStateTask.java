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
 * Time: 1:12 PM
 */
final class RealmChangeStateTask extends MessengerContextTask<Context, Realm> {

    @Nonnull
    static final String TASK_NAME = "realm-change-state";

    @Nonnull
    private final Realm realm;

    RealmChangeStateTask(@Nonnull Realm realm) {
        super(TASK_NAME);
        this.realm = realm;
    }

    @Override
    public Realm call() throws Exception {
        final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();
        return realmService.changeRealmState(realm, realm.isEnabled() ? RealmState.disabled_by_user : RealmState.enabled);
    }

    @Override
    public void onSuccess(@Nonnull Context context, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.status_changed));
    }
}
