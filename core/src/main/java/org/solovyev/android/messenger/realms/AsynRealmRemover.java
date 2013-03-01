package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.List;

/**
* User: serso
* Date: 3/1/13
* Time: 9:18 PM
*/
class AsynRealmRemover extends MessengerAsyncTask<Realm, Integer, List<Realm>> {

    @NotNull
    private final RealmService realmService;

    AsynRealmRemover(@NotNull Activity context,
                     @NotNull RealmService realmService) {
        super(context, true);
        this.realmService = realmService;
    }

    @Override
    protected List<Realm> doWork(@NotNull List<Realm> realms) {
        for (Realm realm : realms) {
            realmService.removeRealm(realm.getId());
        }

        return realms;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable List<Realm> realms) {
        final Context context = getContext();
        if ( context != null && realms != null ) {
            final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
            for (Realm realm : realms) {
                eventManager.fire(new RealmFragmentFinishedEvent(realm, true));
            }
        }
    }
}
