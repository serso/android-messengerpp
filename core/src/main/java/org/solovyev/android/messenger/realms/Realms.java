package org.solovyev.android.messenger.realms;

import android.app.Activity;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.TaskOverlayDialog;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:22 PM
 */
public final class Realms {

    private Realms() {
        throw new AssertionError();
    }

    @Nullable
    static TaskOverlayDialog<?> asyncChangeRealmState(@Nonnull Realm realm, @Nonnull Activity activity) {
        MessengerApplication.getServiceLocator().getTaskService().run(Tasks.toTask(activity, new RealmChangeStateTask(realm)));
        return attachToChangeRealmStateTask(activity);
    }

    @Nullable
    static TaskOverlayDialog<?> attachToChangeRealmStateTask(@Nonnull Activity activity) {
        return TaskOverlayDialog.attachToTask(activity, RealmChangeStateTask.TASK_NAME, R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
    }

    @Nullable
    static TaskOverlayDialog<?> asyncRemoveRealm(@Nonnull Realm realm, @Nonnull Activity activity) {
        MessengerApplication.getServiceLocator().getTaskService().run(Tasks.toTask(activity, new RealmRemoverTask(realm)));
        return attachToRemoveRealmTask(activity);
    }

    @Nullable
    static TaskOverlayDialog<?> attachToRemoveRealmTask(@Nonnull Activity activity) {
        return TaskOverlayDialog.attachToTask(activity, RealmRemoverTask.TASK_NAME, R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
    }

    @Nullable
    static TaskOverlayDialog<?> asyncSaveRealm(RealmBuilder realmBuilder, @Nonnull Activity activity) {
        MessengerApplication.getServiceLocator().getTaskService().run(Tasks.toTask(activity, new RealmSaverTask(realmBuilder)));
        return attachToSaveRealmTask(activity);
    }

    @Nullable
    static TaskOverlayDialog<?> attachToSaveRealmTask(@Nonnull Activity activity) {
        return TaskOverlayDialog.attachToTask(activity, RealmSaverTask.TASK_NAME, R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
    }
}
