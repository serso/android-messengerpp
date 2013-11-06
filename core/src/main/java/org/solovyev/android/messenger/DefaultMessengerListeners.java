/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import com.google.inject.Singleton;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import java.util.Collection;

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
