package org.solovyev.android.messenger.api;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.async.CommonAsyncTask;
import org.solovyev.android.messenger.MessengerCommonActivityImpl;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 4:33 PM
 */
public abstract class MessengerAsyncTask<Param, Progress, R> extends CommonAsyncTask<Param, Progress, R> {

    protected MessengerAsyncTask(@NotNull Context context) {
        super(context);
    }

    protected MessengerAsyncTask(@NotNull Context context, boolean mask) {
        super(context, mask);
    }

    @Override
    protected void onFailurePostExecute(@NotNull Exception e) {
        final Context context = getContext();
        if (context != null) {
            MessengerCommonActivityImpl.handleExceptionStatic(context, e);
        }
    }
}
