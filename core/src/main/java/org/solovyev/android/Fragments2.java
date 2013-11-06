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

package org.solovyev.android;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import javax.annotation.Nonnull;

public class Fragments2 {

	public static void showDialog(@Nonnull android.support.v4.app.DialogFragment dialogFragment,
								  @Nonnull String fragmentTag,
								  @Nonnull android.support.v4.app.FragmentManager fm) {
		showDialog(dialogFragment, fragmentTag, fm, true);
	}

	public static void showDialog(DialogFragment dialogFragment, String fragmentTag, FragmentManager fm, boolean useExisting) {
		android.support.v4.app.Fragment prev = fm.findFragmentByTag(fragmentTag);
		if (prev != null) {
			if (!useExisting) {
				final android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
				ft.remove(prev);
				ft.addToBackStack(null);

				// Create and show the dialog.
				dialogFragment.show(ft, fragmentTag);
				fm.executePendingTransactions();
			}
		} else {
			final android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

			ft.addToBackStack(null);

			// Create and show the dialog.
			dialogFragment.show(ft, fragmentTag);
			fm.executePendingTransactions();
		}
	}
}
