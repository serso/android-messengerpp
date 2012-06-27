package org.solovyev.android.messenger.sync;

import android.content.Context;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.R;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:17 PM
 */
public class TaskIsAlreadyRunningException extends Exception {

    @NotNull
    private SyncTask syncTask;

    public TaskIsAlreadyRunningException(@NotNull SyncTask syncTask) {
        this.syncTask = syncTask;
    }

    @NotNull
    public SyncTask getSyncTask() {
        return syncTask;
    }

    public void showMessage(@NotNull Context c) {
        Toast.makeText(c, R.string.c_task_is_already_running, Toast.LENGTH_SHORT).show();
    }
}
