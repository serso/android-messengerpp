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

import android.app.Activity;
import android.support.v4.app.Fragment;
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

	private final Activity activity;
	private final Fragment fragment;

	@Nonnull
	private final JEventListener<E> eventListener;

	private UiThreadEventListener(Activity activity, Fragment fragment, @Nonnull JEventListener<E> eventListener) {
		this.activity = activity;
		this.fragment = fragment;
		this.eventListener = eventListener;
	}

	@Nonnull
	public static <E extends JEvent> JEventListener<E> onUiThread(@Nonnull Activity activity, @Nonnull JEventListener<E> eventListener) {
		return new UiThreadEventListener<E>(activity, null, eventListener);
	}

	@Nonnull
	public static <E extends JEvent> JEventListener<E> onUiThread(@Nonnull Fragment fragment, @Nonnull JEventListener<E> eventListener) {
		return new UiThreadEventListener<E>(null, fragment, eventListener);
	}

	@Nonnull
	@Override
	public Class<E> getEventType() {
		return eventListener.getEventType();
	}

	@Override
	public void onEvent(@Nonnull final E event) {
		if (activity != null) {
			Threads.tryRunOnUiThread(activity, new Runnable() {
				@Override
				public void run() {
					eventListener.onEvent(event);
				}
			});
		} else {
			Threads2.tryRunOnUiThread(fragment, new Runnable() {
				@Override
				public void run() {
					eventListener.onEvent(event);
				}
			});
		}
	}
}
