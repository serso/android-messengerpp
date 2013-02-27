package org.solovyev.android.messenger;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractRealmConnection<R extends Realm> implements RealmConnection {

    @NotNull
    private static final String TAG = "RealmConnection";

    @NotNull
    private final R realm;

    @NotNull
    private final WeakReference<Context> contextRef;

    @NotNull
    private final AtomicBoolean stopPolling = new AtomicBoolean(false);

    protected AbstractRealmConnection(@NotNull R realm, @NotNull Context context) {
        this.realm = realm;
        this.contextRef = new WeakReference<Context>(context);
    }

    @NotNull
    protected R getRealm() {
        return realm;
    }

    @NotNull
    protected Context getContext() throws ContextIsNotActiveException {
        final Context result = contextRef.get();
        if (result != null) {
            return result;
        } else {
            throw new ContextIsNotActiveException();
        }
    }

    public boolean isStopped() {
        return stopPolling.get();
    }

    @Override
    public final void start() {
        stopPolling.set(false);
        try {
            doWork();
        } catch (ContextIsNotActiveException e) {
            stop();
        }
    }

    protected abstract void doWork() throws ContextIsNotActiveException;

    @Override
    public final void stop() {
        stopPolling.set(true);
    }

    protected final void waitForLogin() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static class ContextIsNotActiveException extends Exception {
    }
}
