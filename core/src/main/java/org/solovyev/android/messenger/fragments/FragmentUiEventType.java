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
