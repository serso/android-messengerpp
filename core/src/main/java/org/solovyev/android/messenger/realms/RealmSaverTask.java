package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.widget.Toast;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerContextTask;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:05 PM
 */
class RealmSaverTask extends MessengerContextTask<Context, Realm> {

    @Nonnull
    static final String TASK_NAME = "realm-save";

    @Nonnull
    private final RealmBuilder realmBuilder;

    RealmSaverTask(@Nonnull RealmBuilder realmBuilder) {
        super(TASK_NAME);
        this.realmBuilder = realmBuilder;
    }

    @Override
    public Realm call() throws InvalidCredentialsException, RealmAlreadyExistsException {
        return MessengerApplication.getServiceLocator().getRealmService().saveRealm(realmBuilder);
    }


    @Override
    public void onSuccess(@Nonnull Context context, Realm realm) {
        final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
        eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.saved));
    }

    @Override
    public void onFailure(@Nonnull Context context, Throwable e) {
        if (e instanceof InvalidCredentialsException) {
            Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show();
        } else if (e instanceof RealmAlreadyExistsException) {
            Toast.makeText(context, "Same account already configured!", Toast.LENGTH_SHORT).show();
        } else {
            super.onFailure(context, e);
        }
    }
}