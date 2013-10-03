package org.solovyev.android.messenger.realms;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.AbstractListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import com.google.inject.Inject;

public class RealmsFragment extends AbstractListFragment<Realm, RealmListItem> implements DetachableFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "realms";

	@Inject
	@Nonnull
	private RealmService realmService;

	public RealmsFragment() {
		super("Realms", false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	protected MessengerListItemAdapter<RealmListItem> createAdapter() {
		final List<RealmListItem> listItems = new ArrayList<RealmListItem>();
		for (Realm realm : realmService.getRealms()) {
			listItems.add(new RealmListItem(realm));
		}
		return new MessengerListItemAdapter<RealmListItem>(getActivity(), listItems);
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<Realm>> createAsyncLoader(@Nonnull MessengerListItemAdapter<RealmListItem> adapter, @Nonnull Runnable onPostExecute) {
		return null;
	}

}