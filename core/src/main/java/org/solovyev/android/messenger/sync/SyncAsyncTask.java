package org.solovyev.android.messenger.sync;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.Realm;

import java.util.List;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:58 PM
 */
class SyncAsyncTask extends MessengerAsyncTask<Void, Void, Void> {

    @NotNull
    private final List<SyncTask> syncTasks;

    public SyncAsyncTask(@NotNull Context context, @NotNull List<SyncTask> syncTasks) {
        super(context);
        this.syncTasks = syncTasks;
    }


    @Override
    protected Void doWork(@NotNull List<Void> voids) {
        for (Realm realm : MessengerApplication.getServiceLocator().getRealmService().getRealms()) {
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
