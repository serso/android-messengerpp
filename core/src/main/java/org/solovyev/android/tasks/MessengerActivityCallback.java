package org.solovyev.android.tasks;

import android.app.Activity;
import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:23 PM
 */
public abstract class MessengerActivityCallback<A extends Activity, V> implements ActivityCallback<A, V> {

    @Override
    public void onFailure(@Nonnull A activity, Throwable t) {
        MessengerApplication.getServiceLocator().getExceptionHandler().handleException(t);
    }
}
