package org.solovyev.android.messenger;

import android.app.Activity;
import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;

import org.solovyev.android.Threads;
import org.solovyev.common.listeners.JEvent;
import org.solovyev.common.listeners.JEventListener;

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
	public static <E extends JEvent> UiThreadEventListener<E> onUiThread(@Nonnull Activity activity, @Nonnull JEventListener<E> eventListener) {
		return new UiThreadEventListener<E>(activity, null, eventListener);
	}

	@Nonnull
	public static <E extends JEvent> UiThreadEventListener<E> onUiThread(@Nonnull Fragment fragment, @Nonnull JEventListener<E> eventListener) {
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
