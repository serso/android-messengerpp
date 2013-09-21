package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:16 PM
 */
public enum FragmentUiEventType {

	/**
	 * After {@link android.app.Fragment#onCreate(android.os.Bundle)} is called
	 */
	created {
		@Override
		@Nonnull
		public FragmentUiEvent newEvent(@Nonnull Fragment fragment) {
			return new FragmentUiEvent(fragment.getClass(), this);
		}
	},

	/**
	 * After {@link android.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)} is called
	 */
	shown {
		@Nonnull
		@Override
		public FragmentUiEvent newEvent(@Nonnull Fragment fragment) {
			return newFragmentEvent(fragment, this);
		}
	},

	started {
		@Nonnull
		@Override
		public FragmentUiEvent newEvent(@Nonnull Fragment fragment) {
			return newFragmentEvent(fragment, this);
		}
	};

	@Nonnull
	public abstract FragmentUiEvent newEvent(@Nonnull Fragment fragment);

	private static FragmentUiEvent newFragmentEvent(@Nonnull Fragment fragment, @Nonnull FragmentUiEventType type) {
		final View view = fragment.getView();
		if (view == null) {
			throw new IllegalArgumentException("View is not created for fragment, but fragment is shown!");
		}

		if (view.getParent() instanceof View) {
			return new FragmentUiEvent(fragment.getClass(), type, ((View) view.getParent()).getId());
		} else {
			return new FragmentUiEvent(fragment.getClass(), type);
		}
	}
}
