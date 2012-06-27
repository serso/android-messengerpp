package org.solovyev.android.messenger.sync;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerCommonActivityImpl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:15 PM
 */
public class DefaultSyncService implements SyncService {

    @NotNull
    private final Set<SyncTask> runningTasks = EnumSet.noneOf(SyncTask.class);

    @Override
    public void syncAll(@NotNull final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SyncTask syncTask : SyncTask.values()) {
                    try {
                        try {
                            checkRunningTask(syncTask);
                            if (syncTask.isTime(context)) {
                                syncTask.doTask(context);
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
        }).start();
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
