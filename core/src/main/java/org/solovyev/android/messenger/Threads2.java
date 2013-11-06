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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import javax.annotation.Nonnull;

import org.solovyev.android.Threads;

/**
 * User: serso
 * Date: 5/29/13
 * Time: 9:34 PM
 */
public final class Threads2 {

	private Threads2() {
		throw new AssertionError();
	}

	public static void tryRunOnUiThread(@Nonnull final Fragment fragment, @Nonnull final Runnable runnable) {
		Threads.tryRunOnUiThread(fragment.getActivity(), new Runnable() {
			@Override
			public void run() {
				final FragmentActivity activity = fragment.getActivity();
				if (activity != null) {
					runnable.run();
				}
			}
		});
	}
}
