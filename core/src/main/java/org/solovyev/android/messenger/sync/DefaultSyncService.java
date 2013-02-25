package org.solovyev.android.messenger.sync;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerCommonActivityImpl;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:15 PM
 */
@Singleton
public class DefaultSyncService implements SyncService {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private RealmService realmService;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    @NotNull
    private final Set<SyncTask> runningTasks = EnumSet.noneOf(SyncTask.class);

    @NotNull
    private final AtomicBoolean syncAllTaskRunning = new AtomicBoolean(false);

    @NotNull
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void syncAll(@NotNull final Context context) throws SyncAllTaskIsAlreadyRunning {

        synchronized (syncAllTaskRunning) {
            if ( syncAllTaskRunning.get() ) {
                throw new SyncAllTaskIsAlreadyRunning();
            } else {
                syncAllTaskRunning.set(true);
            }
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    for (Realm realm : DefaultSyncService.this.realmService.getRealms()) {
                        final SyncData syncData = new SyncDataImpl(realm.getRealmDef().getId());

                        for (SyncTask syncTask : SyncTask.values()) {
                            try {
                                try {
                                    checkRunningTask(syncTask);
                                    if (syncTask.isTime(syncData, context)) {
                                        syncTask.doTask(syncData, context);
                                    }
                                } finally {
                                    releaseRunningTask(syncTask);
                                }
                            } catch (TaskIsAlreadyRunningException e) {
                                // ok, task is already running => start another task
                            } catch (RuntimeException e) {
                                MessengerCommonActivityImpl.handleExceptionStatic(context, e);
                            }
                        }
                    }

                } finally {
                    synchronized (syncAllTaskRunning) {
                        syncAllTaskRunning.set(false);
                    }
                }
            }
        });

    }

    @Override
    public void sync(@NotNull SyncTask syncTask, @NotNull Context context, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException {
        checkRunningTask(syncTask);

        new ServiceSyncAsyncTask(context, syncTask, afterSyncCallback).execute();
    }

    private void checkRunningTask(SyncTask syncTask) throws TaskIsAlreadyRunningException {
        synchronized (runningTasks) {
            if (runningTasks.contains(syncTask)) {
                throw new TaskIsAlreadyRunningException(syncTask);
            }
            runningTasks.add(syncTask);
        }
    }

    private class ServiceSyncAsyncTask extends SyncAsyncTask {

        @NotNull
        private final SyncTask syncTask;

        @Nullable
        private final Runnable afterSyncCallback;

        public ServiceSyncAsyncTask(@NotNull Context context, @NotNull SyncTask syncTask, @Nullable Runnable afterSyncCallback) {
            super(context, Arrays.asList(syncTask));
            this.syncTask = syncTask;
            this.afterSyncCallback = afterSyncCallback;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {
            releaseRunningTask(syncTask);
            super.onSuccessPostExecute(result);

            if (afterSyncCallback != null) {
                afterSyncCallback.run();
            }
        }

        @Override
        protected void onCancelled() {
            // just in case
            releaseRunningTask(syncTask);
            super.onCancelled();
        }

        @Override
        protected void onFailurePostExecute(@NotNull Exception e) {
            releaseRunningTask(syncTask);
            super.onFailurePostExecute(e);
        }
    }

    private void releaseRunningTask(@NotNull SyncTask syncTask) {
        synchronized (runningTasks) {
            runningTasks.remove(syncTask);
        }
    }

}
