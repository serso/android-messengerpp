package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.widget.Toast;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.tasks.MessengerActivityCallback;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:21 PM
 */
final class RealmSaverCallback extends MessengerActivityCallback<Activity, Realm> {

    @Override
    public void onSuccess(@Nonnull Activity activity, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(activity).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.saved));
    }

    @Override
    public void onFailure(@Nonnull Activity activity, Throwable e) {
        if (e instanceof InvalidCredentialsException) {
            Toast.makeText(activity, "Invalid credentials!", Toast.LENGTH_SHORT).show();
        } else if (e instanceof RealmAlreadyExistsException) {
            Toast.makeText(activity, "Same account already configured!", Toast.LENGTH_SHORT).show();
        } else {
            super.onFailure(activity, e);
        }
    }
}
