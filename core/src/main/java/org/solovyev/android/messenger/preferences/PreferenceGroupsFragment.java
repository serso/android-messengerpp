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

import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.BaseStaticListFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class PreferenceGroupsFragment extends BaseStaticListFragment<PreferenceGroupListItem> implements DetachableFragment {

	public static final String FRAGMENT_TAG = "preference-groups";

	public PreferenceGroupsFragment() {
		super(FRAGMENT_TAG, R.string.mpp_preferences, false, true);
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull PreferenceGroupListItem selectedItem) {
		boolean canReuse = false;
		if (fragment instanceof PreferenceListFragment) {
			canReuse = selectedItem.getPreferenceGroup().getPreferencesResId() == ((PreferenceListFragment) fragment).getPreferencesResId();
		}
		return canReuse;
	}

	@Nonnull
	@Override
	protected BaseListItemAdapter<PreferenceGroupListItem> createAdapter() {
		final List<PreferenceGroupListItem> preferences = new ArrayList<PreferenceGroupListItem>();

		preferences.add(new PreferenceGroupListItem(new PreferenceGroup("preferences-appearance", R.string.mpp_preferences_appearance, R.xml.mpp_preferences_appearance, R.drawable.mpp_settings_appearance_states)));
		preferences.add(new PreferenceGroupListItem(new PreferenceGroup("preferences-others", R.string.mpp_preferences_other, R.xml.mpp_preferences_other, R.drawable.mpp_settings_other_states)));

		return new PreferencesAdapter(this.getActivity(), preferences);
	}
}
