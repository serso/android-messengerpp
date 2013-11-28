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

package org.solovyev.android.messenger.preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import roboguice.event.EventListener;

import com.google.inject.Inject;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.RoboListeners;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.sync.SyncService;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.preferences.MessengerPreferenceListFragment.newPreferencesListFragmentDef;

public final class PreferencesActivity extends BaseFragmentActivity implements PreferenceListFragment.OnPreferenceAttachedListener {

	/*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private SyncService syncService;

	public static void start(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, PreferencesActivity.class);
		activity.startActivity(result);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			getMultiPaneFragmentManager().setMainFragment(PrimaryFragment.settings);
		}

		final RoboListeners listeners = getListeners();
		listeners.add(PreferenceUiEvent.Clicked.class, new OnPreferenceClickedListener());

		initFragments();
	}


	@Override
	public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId) {
		new MessengerOnPreferenceAttachedListener(this, syncService).onPreferenceAttached(preferenceScreen, preferenceResId);
	}

	private class OnPreferenceClickedListener implements EventListener<PreferenceUiEvent.Clicked> {
		@Override
		public void onEvent(PreferenceUiEvent.Clicked event) {
			final MessengerMultiPaneFragmentManager fm = getMultiPaneFragmentManager();

			final PreferenceGroup group = event.getGroup();
			final int preferencesResId = group.getPreferencesResId();

			if (isDualPane()) {
				fm.setSecondFragment(newPreferencesListFragmentDef(PreferencesActivity.this, preferencesResId, false));
				if (isTriplePane()) {
					fm.emptifyThirdFragment();
				}
			} else {
				fm.setMainFragment(newPreferencesListFragmentDef(PreferencesActivity.this, preferencesResId, true));
			}
		}
	}
}
