package org.solovyev.android.messenger.api;

import android.content.Context;
import org.solovyev.android.async.CommonAsyncTask;
import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 4:33 PM
 */
public abstract class MessengerAsyncTask<Param, Progress, R> extends CommonAsyncTask<Param, Progress, R> {

    protected MessengerAsyncTask() {
    }

    protected MessengerAsyncTask(@Nonnull Context context) {
        super(context);
    }

    @Override
    protected void onFailurePostExecute(@Nonnull Exception e) {
        MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
    }
}
