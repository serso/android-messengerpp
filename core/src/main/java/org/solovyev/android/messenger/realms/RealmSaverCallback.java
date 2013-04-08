package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.widget.Toast;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.tasks.MessengerContextCallback;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:21 PM
 */
final class RealmSaverCallback extends MessengerContextCallback<Activity, Realm> {

    @Override
    public void onSuccess(@Nonnull Activity context, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.saved));
    }

    @Override
    public void onFailure(@Nonnull Activity context, Throwable e) {
        if (e instanceof InvalidCredentialsException) {
            Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show();
        } else if (e instanceof RealmAlreadyExistsException) {
            Toast.makeText(context, "Same account already configured!", Toast.LENGTH_SHORT).show();
        } else {
            super.onFailure(context, e);
        }
    }
}
