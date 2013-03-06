package org.solovyev.android.messenger.sync;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
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
    @Nonnull
    private RealmService realmService;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    private final Set<SyncTask> runningTasks = EnumSet.noneOf(SyncTask.class);

    @Nonnull
    private final AtomicBoolean syncAllTaskRunning = new AtomicBoolean(false);

    @Nonnull
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void syncAll(final boolean force) throws SyncAllTaskIsAlreadyRunning {
        startSyncAllTask(realmService.getRealms(), force);
    }

    /**
     * Method checks if 'all synchronization task' is not running and starts one with specified parameters
     * @param realms realms for which synchronization should be done
     * @param force force synchronization. See {@link SyncService#syncAll(boolean)}
     * @throws SyncAllTaskIsAlreadyRunning thrown when task if 'all synchronization task' is alreasy running
     */
    private void startSyncAllTask(@Nonnull Collection<Realm> realms, boolean force) throws SyncAllTaskIsAlreadyRunning {
        synchronized (syncAllTaskRunning) {
            if ( syncAllTaskRunning.get() ) {
                throw new SyncAllTaskIsAlreadyRunning();
            } else {
                syncAllTaskRunning.set(true);
            }
        }

        executor.execute(new SyncRunnable(force, realms));
    }

    @Override
    public void syncAllInRealm(@Nonnull Realm realm, boolean force) throws SyncAllTaskIsAlreadyRunning {
        startSyncAllTask(Arrays.asList(realm), force);
    }

    @Override
    public void sync(@Nonnull SyncTask syncTask, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException {
        checkRunningTask(syncTask);

        new ServiceSyncAsyncTask(syncTask, afterSyncCallback).execute();
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

        @Nonnull
        private final SyncTask syncTask;

        @Nullable
        private final Runnable afterSyncCallback;

        public ServiceSyncAsyncTask(@Nonnull SyncTask syncTask, @Nullable Runnable afterSyncCallback) {
            super(Arrays.asList(syncTask));
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
        protected void onFailurePostExecute(@Nonnull Exception e) {
            releaseRunningTask(syncTask);
            super.onFailurePostExecute(e);
        }
    }

    private void releaseRunningTask(@Nonnull SyncTask syncTask) {
        synchronized (runningTasks) {
            runningTasks.remove(syncTask);
        }
    }

    private class SyncRunnable implements Runnable {

        private final boolean force;

        @Nonnull
        private Collection<Realm> realms;

        public SyncRunnable(boolean force, @Nonnull Collection<Realm> realms) {
            this.force = force;
            this.realms = realms;
        }

        @Override
        public void run() {
            try {

                realms = DefaultSyncService.this.realmService.getRealms();
                for (Realm realm : realms) {
                    final SyncData syncData = new SyncDataImpl(realm.getId());

                    for (SyncTask syncTask : SyncTask.values()) {
                        try {
                            try {
                                checkRunningTask(syncTask);
                                if (force || syncTask.isTime(syncData)) {
                                    syncTask.doTask(syncData);
                                }
                            } finally {
                                releaseRunningTask(syncTask);
                            }
                        } catch (TaskIsAlreadyRunningException e) {
                            // ok, task is already running => start another task
                        } catch (RuntimeException e) {
                            MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
                        }
                    }
                }

            } finally {
                synchronized (syncAllTaskRunning) {
                    syncAllTaskRunning.set(false);
                }
            }
        }
    }
}
