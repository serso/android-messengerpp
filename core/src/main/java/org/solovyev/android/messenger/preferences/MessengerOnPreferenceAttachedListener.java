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

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 7:35 PM
 */
public final class MessengerOnPreferenceAttachedListener implements PreferenceListFragment.OnPreferenceAttachedListener {

	@Nonnull
	private final Context context;

	@Nonnull
	private final SyncService syncService;

	public MessengerOnPreferenceAttachedListener(@Nonnull Context context, @Nonnull SyncService syncService) {
		this.context = context;
		this.syncService = syncService;
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId) {
		if (preferenceResId == R.xml.mpp_preferences_others) {
			onOtherPreferencesAttached(preferenceScreen);
		}
	}

	private void onOtherPreferencesAttached(PreferenceScreen preferenceScreen) {
		final Preference reloadData = preferenceScreen.findPreference("reload_data");

		reloadData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// todo serso: show user message that action has been started
				SyncAllAsyncTask.newForAllAccounts(context, syncService).executeInParallel((Void) null);
				return true;
			}
		});
	}
}
