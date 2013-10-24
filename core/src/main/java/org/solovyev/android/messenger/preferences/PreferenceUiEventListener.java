package org.solovyev.android.messenger.preferences;

import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;

import static org.solovyev.android.messenger.preferences.MainPreferenceListFragment.newPreferencesListFragmentDef;

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
				fm.setSecondFragment(newPreferencesListFragmentDef(activity, preferencesResId, false));
				if (activity.isTriplePane()) {
					fm.emptifyThirdFragment();
				}
			} else {
				fm.setMainFragment(newPreferencesListFragmentDef(activity, preferencesResId, true));
			}
		}
	}
}
