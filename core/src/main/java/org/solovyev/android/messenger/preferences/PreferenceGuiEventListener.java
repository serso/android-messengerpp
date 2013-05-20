package org.solovyev.android.messenger.preferences;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:28 PM
 */
public final class PreferenceGuiEventListener implements EventListener<PreferenceGuiEvent> {

	@Nonnull
	private final MessengerFragmentActivity activity;

	public PreferenceGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull PreferenceGuiEvent event) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();
		final PreferenceGroup preferenceGroup = event.getPreferenceScreen();

		if (event.isOfType(PreferenceGuiEventType.preference_group_clicked)) {
			final int preferencesResId = preferenceGroup.getPreferencesResId();

			if (activity.isDualPane()) {
				fm.setSecondFragment(new Builder<Fragment>() {
					@Nonnull
					@Override
					public Fragment build() {
						return new MessengerPreferenceListFragment(preferencesResId);
					}
				}, PreferenceListFragmentReuseCondition.newInstance(preferencesResId), PreferenceListFragment.FRAGMENT_TAG);
				if (activity.isTriplePane()) {
					fm.emptifyThirdFragment();
				}
			} else {
				fm.setMainFragment(new Builder<Fragment>() {
					@Nonnull
					@Override
					public Fragment build() {
						return new MessengerPreferenceListFragment(preferencesResId);
					}
				}, PreferenceListFragmentReuseCondition.newInstance(preferencesResId), PreferenceListFragment.FRAGMENT_TAG, true);
			}
		}
	}
}
