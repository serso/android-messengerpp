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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.sherlock.AndroidSherlockUtils.getSupportActionBar;

public class ActivityUi implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Nonnull
	private static final String TAG = "ActivityUi";

	/*
	**********************************************************************
	*
	*                           AUTO INJECTED
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private EventManager eventManager;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final Activity activity;

	@Nonnull
	private MessengerTheme theme = MessengerTheme.holo;

	private RoboListeners listeners;

	private final boolean dialog;

	public ActivityUi(@Nonnull Activity activity) {
		this(activity, false);
	}

	public ActivityUi(@Nonnull Activity activity, boolean dialog) {
		this.activity = activity;
		this.dialog = dialog;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		tryUpdateTheme();
	}

	private void tryUpdateTheme() {
		final MessengerTheme newTheme = App.getThemeFromPreferences();
		if (!newTheme.equals(theme)) {
			theme = newTheme;
			restartActivity();
		}
	}

	protected void restartActivity() {
		Activities.restartActivity(activity);
	}

	/*
	**********************************************************************
	*
	*                           LIFECYCLE
	*
	**********************************************************************
	*/

	public void onBeforeCreate() {
		theme = App.getTheme();
		activity.setTheme(dialog ? theme.getDialogThemeResId() : theme.getThemeResId());

		prepareActionBar();
	}

	private void prepareActionBar() {
		final ActionBar actionBar = getActionBar();
		// if activity is a dialog - no action bar will be provided
		if (actionBar != null) {
			actionBar.setIcon(theme.getActionBarIconResId());
		}
	}

	@Nullable
	private ActionBar getActionBar() {
		if (!dialog) {
			try {
				return getSupportActionBar(activity);
			} catch (IllegalStateException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return null;
	}

	public void onCreate(@Nullable Bundle savedInstanceState) {
		RoboGuice.getInjector(activity).injectMembers(this);
		listeners = new RoboListeners(getEventManager());
	}

	public void onResume() {
		tryUpdateTheme();
		App.getPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onPause() {
		App.getPreferences().unregisterOnSharedPreferenceChangeListener(this);

		if (listeners != null) {
			listeners.clearAll();
		}
	}

	/*
	**********************************************************************
	*
	*                           GETTERS
	*
	**********************************************************************
	*/

	@Nonnull
	public RoboListeners getListeners() {
		return listeners;
	}

	@Nonnull
	public EventManager getEventManager() {
		return eventManager;
	}

	@Nonnull
	protected Activity getActivity() {
		return activity;
	}
}
