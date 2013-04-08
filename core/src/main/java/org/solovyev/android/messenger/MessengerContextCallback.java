package org.solovyev.android.messenger;

import android.app.Activity;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:23 PM
 */
public abstract class MessengerContextCallback<A extends Activity, V> implements ContextCallback<A, V> {

    @Override
    public void onFailure(@Nonnull A context, Throwable t) {
        MessengerApplication.getServiceLocator().getExceptionHandler().handleException(t);
    }
}
