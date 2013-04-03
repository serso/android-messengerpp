package org.solovyev.android.messenger.realms;

import android.app.Activity;
import org.solovyev.android.tasks.MessengerActivityCallback;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:34 PM
 */
final class RealmRemoverCallback extends MessengerActivityCallback<Activity, Realm> {

    @Override
    public void onSuccess(@Nonnull Activity activity, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(activity).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.removed));
    }
}
