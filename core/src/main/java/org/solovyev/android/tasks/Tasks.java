package org.solovyev.android.tasks;

import android.app.Activity;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.Threads;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 9:59 PM
 */
public final class Tasks {

    private Tasks() {
        throw new AssertionError();
    }

    @Nonnull
    public static <A extends Activity, V> FutureCallback<V> newUiThreadCallback(@Nonnull A activity, @Nonnull ActivityCallback<A, V> callback) {
        return new UiThreadCallback<A, V>(activity, callback);
    }

    @Nonnull
    public static <A extends Activity, V> FutureCallback<V> newFutureCallback(@Nonnull A activity, @Nonnull ActivityCallback<A, V> callback) {
        return new CallbackAdapter<A, V>(activity, callback);
    }

    private static final class UiThreadCallback<C extends Activity, V> extends ContextCallback<V, C> {

        @Nonnull
        private final ActivityCallback<C, V> callback;

        public UiThreadCallback(@Nonnull C activity, @Nonnull ActivityCallback<C, V> callback) {
            super(activity);
            this.callback = callback;
        }

        @Override
        public void onSuccess(final V result) {
            final C activity = getContext();
            Threads.tryRunOnUiThread(activity, new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(activity, result);
                }
            });
        }

        @Override
        public void onFailure(final Throwable t) {
            final C activity = getContext();
            Threads.tryRunOnUiThread(activity, new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(activity, t);
                }
            });
        }
    }

    private static final class CallbackAdapter<C extends Activity, V> extends ContextCallback<V, C> {

        @Nonnull
        private final ActivityCallback<C, V> callback;

        public CallbackAdapter(@Nonnull C activity, @Nonnull ActivityCallback<C, V> callback) {
            super(activity);
            this.callback = callback;
        }

        @Override
        public void onSuccess(final V result) {
            final C activity = getContext();
            if ( activity != null ) {
                callback.onSuccess(activity, result);
            }
        }

        @Override
        public void onFailure(final Throwable t) {
            final C activity = getContext();
            if ( activity != null ) {
                callback.onFailure(activity, t);
            }
        }
    }
}
