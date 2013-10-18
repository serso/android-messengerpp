package org.solovyev.android.messenger.preferences;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.AbstractListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 5:56 PM
 */
public final class PreferenceGroupsFragment extends AbstractListFragment<PreferenceGroup, PreferenceGroupListItem> implements DetachableFragment {

	public static final String FRAGMENT_TAG = "preference-groups";

	public PreferenceGroupsFragment() {
		super(FRAGMENT_TAG, false, true);
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
	protected boolean isSupportedSecondFragment(@Nonnull Fragment fragment) {
		return fragment instanceof PreferenceListFragment;
	}

	@Nonnull
	@Override
	protected MessengerListItemAdapter<PreferenceGroupListItem> createAdapter() {
		final List<PreferenceGroupListItem> preferences = new ArrayList<PreferenceGroupListItem>();

		preferences.add(new PreferenceGroupListItem(new PreferenceGroup("preferences-appearance", R.string.mpp_settings_appearance, R.xml.mpp_preferences_appearance, R.drawable.mpp_icon_settings_appearance)));
		preferences.add(new PreferenceGroupListItem(new PreferenceGroup("preferences-others", R.string.mpp_settings_other, R.xml.mpp_preferences_others, R.drawable.mpp_icon_settings_other)));

		return new PreferencesAdapter(this.getActivity(), preferences);
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<PreferenceGroup>> createAsyncLoader(@Nonnull MessengerListItemAdapter<PreferenceGroupListItem> adapter, @Nonnull Runnable onPostExecute) {
		return null;
	}
}
