package org.solovyev.android.messenger.sync;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.RealmDef;

import java.util.List;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:58 PM
 */
class SyncAsyncTask extends MessengerAsyncTask<Void, Void, Void> {

    @Nonnull
    private final List<SyncTask> syncTasks;

    public SyncAsyncTask(@Nonnull Context context, @Nonnull List<SyncTask> syncTasks) {
        super(context);
        this.syncTasks = syncTasks;
    }


    @Override
    protected Void doWork(@Nonnull List<Void> voids) {
        for (RealmDef realm : MessengerApplication.getServiceLocator().getRealmService().getRealmDefs()) {
            final SyncData syncData = new SyncDataImpl(realm.getId());

            for (SyncTask syncTask : syncTasks) {
                final Context context = getContext();
                if (context != null) {
                    syncTask.doTask(syncData, context);
                }
            }
        }

        return null;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable Void result) {
    }
}
