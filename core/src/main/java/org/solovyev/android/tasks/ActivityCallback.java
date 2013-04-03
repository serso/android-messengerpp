package org.solovyev.android.tasks;

import android.app.Activity;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:10 PM
 */
public interface ActivityCallback<A extends Activity, V> {

    void onSuccess(@Nonnull A activity, V result);

    void onFailure(@Nonnull A activity, Throwable t);
}
