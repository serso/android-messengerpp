package org.solovyev.android.messenger;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractRealmConnection<R extends Realm> implements RealmConnection {

    @Nonnull
    private static final String TAG = "RealmConnection";

    @Nonnull
    private final R realm;

    @Nonnull
    private final WeakReference<Context> contextRef;

    @Nonnull
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    protected AbstractRealmConnection(@Nonnull R realm, @Nonnull Context context) {
        this.realm = realm;
        this.contextRef = new WeakReference<Context>(context);
    }

    @Nonnull
    public final R getRealm() {
        return realm;
    }

    @Nonnull
    protected Context getContext() throws ContextIsNotActiveException {
        final Context result = contextRef.get();
        if (result != null) {
            return result;
        } else {
            throw new ContextIsNotActiveException();
        }
    }

    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public final void start() {
        stopped.set(false);
        try {
            doWork();
        } catch (ContextIsNotActiveException e) {
            // do nothing
        } finally {
            stop();
        }
    }

    protected abstract void doWork() throws ContextIsNotActiveException;
    protected abstract void stopWork();

    @Override
    public final void stop() {
        stopped.set(true);
        stopWork();
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
