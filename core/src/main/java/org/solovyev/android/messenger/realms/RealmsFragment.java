package org.solovyev.android.messenger.realms;

import android.support.v4.app.Fragment;
import com.google.inject.Inject;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.BaseListFragment;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RealmsFragment extends BaseListFragment<Realm, RealmListItem> implements DetachableFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "realms";

	@Inject
	@Nonnull
	private RealmService realmService;

	public RealmsFragment() {
		super("Realms", false, true);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull RealmListItem selectedItem) {
		boolean canReuse = false;
		if (fragment instanceof BaseAccountConfigurationFragment) {
			canReuse = ((BaseAccountConfigurationFragment) fragment).getRealm().equals(selectedItem.getRealm());
		}
		return canReuse;
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
	protected BaseListItemAdapter<RealmListItem> createAdapter() {
		final List<RealmListItem> listItems = new ArrayList<RealmListItem>();
		for (Realm realm : realmService.getRealms()) {
			listItems.add(new RealmListItem(realm));
		}
		return new BaseListItemAdapter<RealmListItem>(getActivity(), listItems);
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<Realm>> createAsyncLoader(@Nonnull BaseListItemAdapter<RealmListItem> adapter, @Nonnull Runnable onPostExecute) {
		return null;
	}

}