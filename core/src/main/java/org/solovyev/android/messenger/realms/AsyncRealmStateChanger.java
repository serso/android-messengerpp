package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
* User: serso
* Date: 3/1/13
* Time: 9:18 PM
*/
class AsyncRealmStateChanger extends MessengerAsyncTask<Realm, Integer, List<Realm>> {


    AsyncRealmStateChanger(@Nonnull Activity context) {
        super(context, MaskParams.newInstance(R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message));
    }

    @Override
    protected List<Realm> doWork(@Nonnull List<Realm> realms) {
        final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();

        for (Realm realm : realms) {
            realmService.changeRealmState(realm, realm.isEnabled() ? RealmState.disabled_by_user : RealmState.enabled );
        }

        return realms;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable List<Realm> realms) {
        final Context context = getContext();
        if ( context != null && realms != null ) {
            final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
            for (Realm realm : realms) {
                eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.status_changed));
            }
        }
    }
}
