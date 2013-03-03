package org.solovyev.android.messenger.sync;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:14 PM
 */
public interface SyncService {

    void syncAll(@Nonnull Context context) throws SyncAllTaskIsAlreadyRunning;

    void sync(@Nonnull SyncTask syncTask, @Nonnull Context context, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException;
}
