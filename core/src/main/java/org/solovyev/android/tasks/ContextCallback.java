package org.solovyev.android.tasks;

import android.content.Context;
import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:02 PM
 */
abstract class ContextCallback<V, C extends Context> implements FutureCallback<V> {

    @Nonnull
    private final WeakReference<C> contextRef;

    ContextCallback(@Nonnull C context) {
        this.contextRef = new WeakReference<C>(context);
    }

    @Nullable
    protected C getContext() {
        return contextRef.get();
    }
}
