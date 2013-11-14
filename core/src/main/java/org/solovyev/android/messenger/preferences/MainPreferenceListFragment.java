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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.inject.Inject;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.core.R;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MainPreferenceListFragment extends PreferenceListFragment {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED VIEWS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	public MainPreferenceListFragment() {
		super();
	}

	@Nonnull
	public static MultiPaneFragmentDef newPreferencesListFragmentDef(@Nonnull Context context, int preferencesResId, boolean addToBackStack) {
		final Bundle arguments = newPreferencesArguments(preferencesResId, R.layout.mpp_fragment_preferences, R.style.mpp_theme_holo_fragment);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, MainPreferenceListFragment.class, context, arguments, PreferenceListFragmentReuseCondition.newInstance(preferencesResId));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RoboGuice.getInjector(this.getActivity()).injectMembersWithoutViews(this);
	}

	@Override
	protected void prepareListView(@Nonnull ListView lv) {
		super.prepareListView(lv);
		lv.setBackgroundDrawable(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			lv.setOverscrollFooter(null);
		}
	}

	@Override
	protected void onCreateView(@Nonnull Context context, @Nonnull LayoutInflater inflater, @Nonnull View root, @Nonnull ViewGroup container, @Nullable Bundle b) {
		super.onCreateView(context, inflater, root, container, b);

		multiPaneManager.onCreatePane(getActivity(), container, root);
		final int padding = context.getResources().getDimensionPixelSize(R.dimen.mpp_fragment_padding);
		root.setPadding(padding, padding, padding, padding);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final PreferenceScreen preferenceScreen = getPreferenceScreen();
		if (preferenceScreen != null) {
			multiPaneManager.showTitle((SherlockFragmentActivity) getActivity(), root, preferenceScreen.getTitle());
		}
	}
}
