package org.solovyev.android.messenger;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 3/23/13
 * Time: 5:26 PM
 */
@Singleton
public final class DefaultMessengerListeners implements MessengerListeners {

	@Nonnull
	private final JEventListeners<JEventListener<? extends MessengerEvent>, MessengerEvent> listeners = Listeners.newEventBusFor(MessengerEvent.class);

	@Override
	public void fireEvent(@Nonnull MessengerEvent event) {
		listeners.fireEvent(event);
	}

	@Override
	public void fireEvents(@Nonnull Collection<MessengerEvent> events) {
		listeners.fireEvents(events);
	}

	public boolean addListener(@Nonnull JEventListener<MessengerEvent> listener) {
		return listeners.addListener(listener);
	}

	public boolean removeListener(@Nonnull JEventListener<MessengerEvent> listener) {
		return listeners.removeListener(listener);
	}

	@Override
	public void removeListeners() {
		listeners.removeListeners();
	}
}
