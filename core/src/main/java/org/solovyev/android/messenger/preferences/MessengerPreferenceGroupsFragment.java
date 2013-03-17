package org.solovyev.android.messenger.preferences;

import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.DetachableFragment;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 5:56 PM
 */
public final class MessengerPreferenceGroupsFragment extends AbstractMessengerListFragment<PreferenceGroup, PreferenceGroupListItem> implements DetachableFragment {

    public static final String FRAGMENT_TAG = "preference-groups";

    public MessengerPreferenceGroupsFragment() {
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

    @Nonnull
    @Override
    protected MessengerListItemAdapter<PreferenceGroupListItem> createAdapter() {
        final List<PreferenceGroupListItem> preferences = new ArrayList<PreferenceGroupListItem>();

        preferences.add(new PreferenceGroupListItem(new PreferenceGroup("test", "test", R.xml.mpp_preferences_appearance)));
        preferences.add(new PreferenceGroupListItem(new PreferenceGroup("test2", "test2", R.xml.mpp_preferences_others)));

        return new PreferencesAdapter(this.getActivity(), preferences);
    }

    @Nullable
    @Override
    protected MessengerAsyncTask<Void, Void, List<PreferenceGroup>> createAsyncLoader(@Nonnull MessengerListItemAdapter<PreferenceGroupListItem> adapter, @Nonnull Runnable onPostExecute) {
        return null;
    }
}
