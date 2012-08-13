package org.solovyev.android.messenger.sync;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:14 PM
 */
public interface SyncService {

    void syncAll(@NotNull Context context) throws SyncAllTaskIsAlreadyRunning;

    void sync(@NotNull SyncTask syncTask, @NotNull Context context, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException;
}
