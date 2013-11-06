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

import roboguice.event.EventListener;
import roboguice.event.EventManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

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

	public <T> void remove(@Nonnull Class<T> eventClass, @Nonnull EventListener<T> listener) {
		eventManager.unregisterObserver(eventClass, listener);
	}

	public void clearAll() {
		for (EventEventListener listener : listeners) {
			eventManager.unregisterObserver(listener.eventClass, listener.eventListener);
		}

		listeners.clear();
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
