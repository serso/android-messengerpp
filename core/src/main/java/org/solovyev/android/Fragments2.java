package org.solovyev.android;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import javax.annotation.Nonnull;

public class Fragments2 {

	public static void showDialog(@Nonnull android.support.v4.app.DialogFragment dialogFragment,
								  @Nonnull String fragmentTag,
								  @Nonnull android.support.v4.app.FragmentManager fm) {
		showDialog(dialogFragment, fragmentTag, fm, true, true);
	}

	public static void showDialog(DialogFragment dialogFragment, String fragmentTag, FragmentManager fm, boolean useExisting, boolean addToBackStack) {
		android.support.v4.app.Fragment prev = fm.findFragmentByTag(fragmentTag);
		if (prev != null) {
			if (!useExisting) {
				final android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
				ft.remove(prev);
				if (addToBackStack) {
					ft.addToBackStack(null);
				}

				// Create and show the dialog.
				dialogFragment.show(ft, fragmentTag);
				fm.executePendingTransactions();
			}
		} else {
			final android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

			if (addToBackStack) {
				ft.addToBackStack(null);
			}

			// Create and show the dialog.
			dialogFragment.show(ft, fragmentTag);
			fm.executePendingTransactions();
		}
	}
}
