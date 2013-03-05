package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* User: serso
* Date: 3/1/13
* Time: 9:18 PM
*/
class AsynRealmSaver extends MessengerAsyncTask<RealmBuilder, Integer, List<Realm>> {

    @Nonnull
    private final RealmService realmService;

    @Nullable
    private final BaseRealmConfigurationFragment.RealmSaveHandler realmSaveHandler;

    AsynRealmSaver(@Nonnull Activity context,
                   @Nonnull RealmService realmService,
                   @Nullable BaseRealmConfigurationFragment.RealmSaveHandler realmSaveHandler) {
        super(context, true);
        this.realmService = realmService;
        this.realmSaveHandler = realmSaveHandler;
    }

    @Override
    protected List<Realm> doWork(@Nonnull List<RealmBuilder> realmBuilders) {
        final List<Realm> result = new ArrayList<Realm>(realmBuilders.size());
        for (RealmBuilder realmBuilder : realmBuilders) {
            try {
                result.add(realmService.saveRealm(realmBuilder));
            } catch (InvalidCredentialsException e) {
                throwException(e);
            } catch (RealmAlreadyExistsException e) {
                throwException(e);
            }
        }

        return result;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable List<Realm> realms) {
        final Context context = getContext();
        if (context != null && realms != null) {
            final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);

            final Set<RealmDef> realmDefs = new HashSet<RealmDef>();
            for (Realm realm : realms) {
                realmDefs.add(realm.getRealmDef());
                eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, false));
            }

            for (RealmDef realmDef : realmDefs) {
                eventManager.fire(RealmDefGuiEventType.newRealmDefEditFinishedEvent(realmDef));
            }
        }
    }

    @Override
    protected void onFailurePostExecute(@Nonnull Exception e) {
        boolean consumed = realmSaveHandler != null && realmSaveHandler.onFailure(e);
        if (!consumed) {
            if (e instanceof InvalidCredentialsException) {
                Toast.makeText(getContext(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
                Log.e("XmppRealm", e.getMessage(), e);
            } else if (e instanceof RealmAlreadyExistsException) {
                Toast.makeText(getContext(), "Same account alraedy configured!", Toast.LENGTH_SHORT).show();
                Log.e("XmppRealm", e.getMessage(), e);
            } else {
                super.onFailurePostExecute(e);
            }
        }
    }
}
