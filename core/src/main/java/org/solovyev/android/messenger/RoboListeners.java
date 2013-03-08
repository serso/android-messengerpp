package org.solovyev.android.messenger;

import roboguice.event.EventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:47 PM
 */
public class RoboListeners {

    @Nonnull
    private final List<EventEventListener> listeners = new ArrayList<EventEventListener>();

    @Nonnull
    private final EventManager eventManager;

    public RoboListeners(@Nonnull EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public <T> void add(@Nonnull Class<T> eventClass, @Nonnull EventListener<T> listener) {
        eventManager.registerObserver(eventClass, listener);
        listeners.add(new EventEventListener(eventClass, listener));
    }

    public void clearAll() {
        for (EventEventListener listener : listeners) {
            eventManager.unregisterObserver(listener.eventClass, listener.eventListener);
        }
    }

    private static final class EventEventListener {

        @Nonnull
        private final Class eventClass;

        @Nonnull
        private final EventListener eventListener;

        private EventEventListener(@Nonnull Class eventClass, @Nonnull EventListener eventListener) {
            this.eventClass = eventClass;
            this.eventListener = eventListener;
        }
    }
}
