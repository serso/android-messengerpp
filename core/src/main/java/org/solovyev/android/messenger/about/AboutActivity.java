package org.solovyev.android.messenger.about;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.RoboListeners;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;

public class AboutActivity extends BaseFragmentActivity {

	public static void start(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, AboutActivity.class);
		activity.startActivity(result);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			getMultiPaneFragmentManager().setMainFragment(PrimaryFragment.about);
		}

		final RoboListeners listeners = getListeners();
		listeners.add(AboutUiEvent.Clicked.class, new OnAboutClickedListener());

		initFragments();
	}

	private class OnAboutClickedListener implements EventListener<AboutUiEvent.Clicked> {

		@Override
		public void onEvent(AboutUiEvent.Clicked event) {
			final MessengerMultiPaneFragmentManager fm = getMultiPaneFragmentManager();

			final AboutType type = event.getType();
			if (isDualPane()) {
				fm.setSecondFragment(type.newFragmentDef(AboutActivity.this, false));
				if (isTriplePane()) {
					fm.emptifyThirdFragment();
				}
			} else {
				fm.setMainFragment(type.newFragmentDef(AboutActivity.this, true));
			}
		}
	}
}
