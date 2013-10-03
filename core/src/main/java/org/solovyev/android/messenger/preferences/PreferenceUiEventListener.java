package org.solovyev.android.messenger.preferences;

import android.support.v4.app.Fragment;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.common.Builder;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:28 PM
 */
public final class PreferenceUiEventListener implements EventListener<PreferenceUiEvent> {

	@Nonnull
	private final BaseFragmentActivity activity;

	public PreferenceUiEventListener(@Nonnull BaseFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull PreferenceUiEvent event) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();
		final PreferenceGroup preferenceGroup = event.getPreferenceScreen();

		if (event.isOfType(PreferenceUiEventType.preference_group_clicked)) {
			final int preferencesResId = preferenceGroup.getPreferencesResId();

			if (activity.isDualPane()) {
				fm.setSecondFragment(new Builder<Fragment>() {
					@Nonnull
					@Override
					public Fragment build() {
						return new MainPreferenceListFragment(preferencesResId);
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
						return new MainPreferenceListFragment(preferencesResId);
					}
				}, PreferenceListFragmentReuseCondition.newInstance(preferencesResId), PreferenceListFragment.FRAGMENT_TAG, true);
			}
		}
	}
}
