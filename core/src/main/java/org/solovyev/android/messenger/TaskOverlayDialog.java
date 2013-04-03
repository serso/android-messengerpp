package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.ProgressDialog;
import org.solovyev.android.tasks.ActivityCallback;
import org.solovyev.android.tasks.NoSuchTaskException;
import org.solovyev.android.tasks.TaskFinishedException;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* User: serso
* Date: 4/3/13
* Time: 11:01 PM
*/
public final class TaskOverlayDialog<V> implements ActivityCallback<Activity, V> {

    @Nonnull
    private final ProgressDialog progressDialog;

    private volatile boolean finished = false;

    private TaskOverlayDialog(@Nonnull ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Nonnull
    private static <V> TaskOverlayDialog<V> newInstance(@Nonnull Activity activity, int titleResId, int messageResId) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(titleResId);
        progressDialog.setMessage(activity.getText(messageResId));
        return new TaskOverlayDialog<V>(progressDialog);
    }

    @Nullable
    public static TaskOverlayDialog<?> attachToTask(@Nonnull Activity activity, @Nonnull String taskName, int titleResId, int messageResId) {
        TaskOverlayDialog<Object> taskOverlayDialog = newInstance(activity, titleResId, messageResId);
        try {
            MessengerApplication.getServiceLocator().getAsyncTaskService().addListener(taskName, Tasks.newFutureCallback(activity, taskOverlayDialog));
            // attached to task => can show dialog
            taskOverlayDialog.show();
        } catch (NoSuchTaskException e) {
            taskOverlayDialog = null;
        } catch (TaskFinishedException e) {
            taskOverlayDialog = null;
        }

        return taskOverlayDialog;
    }


    @Override
    public void onSuccess(@Nonnull Activity activity, V result) {
        dismiss();
    }

    public synchronized void dismiss() {
        finished = true;
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public synchronized void show() {
        if (!finished) {
            progressDialog.show();
        }
    }

    @Override
    public void onFailure(@Nonnull Activity activity, Throwable t) {
        dismiss();
    }
}
