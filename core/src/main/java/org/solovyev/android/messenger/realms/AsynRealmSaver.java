package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
        super(context, MaskParams.newInstance(R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message));
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

            for (Realm realm : realms) {
                eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.saved));
            }
        }
    }

    @Override
    protected void onFailurePostExecute(@Nonnull Exception e) {
        boolean consumed = realmSaveHandler != null && realmSaveHandler.onFailure(e);
        if (!consumed) {
            if (e instanceof InvalidCredentialsException) {
                Toast.makeText(getContext(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
            } else if (e instanceof RealmAlreadyExistsException) {
                Toast.makeText(getContext(), "Same account already configured!", Toast.LENGTH_SHORT).show();
            } else {
                super.onFailurePostExecute(e);
            }
        }
    }
}
