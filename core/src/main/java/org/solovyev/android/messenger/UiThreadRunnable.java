package org.solovyev.android.messenger;

import android.app.Activity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 2:23 AM
 */
public class UiThreadRunnable implements Runnable {

    @Nullable
    private final Activity activity;

    @NotNull
    private final Runnable action;

    public UiThreadRunnable(@Nullable Activity activity, @NotNull Runnable runnable) {
        this.activity = activity;
        this.action = runnable;
    }

    @Override
    public void run() {
        if (this.activity != null) {
            this.activity.runOnUiThread(action);
        }
    }
}
