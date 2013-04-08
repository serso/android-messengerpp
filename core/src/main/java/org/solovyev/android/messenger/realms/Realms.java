package org.solovyev.android.messenger.realms;

import android.app.Activity;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.TaskOverlayDialog;
import org.solovyev.android.messenger.core.R;
import org.solovyev.tasks.Tasks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:22 PM
 */
public final class Realms {

    @Nonnull
    private static final String TASK_REALM_SAVE = "realm-save";

    @Nonnull
    private static final String TASK_REALM_REMOVE = "realm-remove";

    private Realms() {
        throw new AssertionError();
    }

    static boolean isRemoveTaskRunning() {
        return MessengerApplication.getServiceLocator().getTaskService().isRunning(TASK_REALM_REMOVE);
    }

    static boolean isSaveTaskRunning() {
        return MessengerApplication.getServiceLocator().getTaskService().isRunning(TASK_REALM_SAVE);
    }

    @Nullable
    static TaskOverlayDialog<?> asyncRemoveRealm(@Nonnull Realm realm, @Nonnull Activity activity) {
        MessengerApplication.getServiceLocator().getTaskService().run(TASK_REALM_REMOVE, new RealmRemoverCallable(realm), Tasks.toUiThreadFutureCallback(activity, new RealmRemoverCallback()));
        return attachToRemoveTask(activity);
    }

    @Nullable
    static TaskOverlayDialog<?> attachToRemoveTask(@Nonnull Activity activity) {
        return TaskOverlayDialog.attachToTask(activity, TASK_REALM_REMOVE, R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
    }

    @Nullable
    static TaskOverlayDialog<?> asyncSaveRealm(RealmBuilder realmBuilder, @Nonnull Activity activity) {
        MessengerApplication.getServiceLocator().getTaskService().run(TASK_REALM_SAVE, new RealmSaverCallable(realmBuilder), Tasks.toUiThreadFutureCallback(activity, new RealmSaverCallback()));
        return attachToSaveTask(activity);
    }

    @Nullable
    static TaskOverlayDialog<?> attachToSaveTask(@Nonnull Activity activity) {
        return TaskOverlayDialog.attachToTask(activity, TASK_REALM_SAVE, R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
    }
}
