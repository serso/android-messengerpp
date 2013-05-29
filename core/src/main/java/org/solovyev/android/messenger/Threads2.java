package org.solovyev.android.messenger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import org.solovyev.android.Threads;

import javax.annotation.Nonnull;

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
