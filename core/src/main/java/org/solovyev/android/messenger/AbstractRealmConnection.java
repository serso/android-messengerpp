package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractRealmConnection implements RealmConnection {

    @NotNull
    private final Realm realm;

    @NotNull
    private final WeakReference<Context> contextRef;

    @NotNull
    private final AtomicBoolean stopPolling = new AtomicBoolean(false);

    protected AbstractRealmConnection(@NotNull Realm realm, @NotNull Context context) {
        this.realm = realm;
        this.contextRef = new WeakReference<Context>(context);
    }

    @NotNull
    protected Realm getRealm() {
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

    @NotNull
    protected ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    protected boolean isStopped() {
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

    public static class ContextIsNotActiveException extends Exception {
    }
}
