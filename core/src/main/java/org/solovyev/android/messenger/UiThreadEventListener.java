package org.solovyev.android.messenger;

import android.app.Activity;
import org.solovyev.android.Threads;
import org.solovyev.common.listeners.JEvent;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/23/13
 * Time: 6:47 PM
 */
public final class UiThreadEventListener<E extends JEvent> implements JEventListener<E> {

    @Nonnull
    private final Activity activity;

    @Nonnull
    private final JEventListener<E> eventListener;

    private UiThreadEventListener(@Nonnull Activity activity, @Nonnull JEventListener<E> eventListener) {
        this.activity = activity;
        this.eventListener = eventListener;
    }

    @Nonnull
    public static <E extends JEvent> UiThreadEventListener<E> wrap(@Nonnull Activity activity, @Nonnull JEventListener<E> eventListener) {
        return new UiThreadEventListener<E>(activity, eventListener);
    }

    @Nonnull
    @Override
    public Class<E> getEventType() {
        return eventListener.getEventType();
    }

    @Override
    public void onEvent(@Nonnull final E event) {
        Threads.tryRunOnUiThread(activity, new Runnable() {
            @Override
            public void run() {
                eventListener.onEvent(event);
            }
        });
    }
}
