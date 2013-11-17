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

package org.solovyev.android.messenger.realms;

import android.support.v4.app.Fragment;
import com.google.inject.Inject;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.BaseStaticListFragment;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RealmsFragment extends BaseStaticListFragment<RealmListItem> implements DetachableFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "realms";

	@Inject
	@Nonnull
	private RealmService realmService;

	public RealmsFragment() {
		super("Realms", R.string.mpp_accounts, false, true);
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
			if (realm.isEnabled()) {
				listItems.add(new RealmListItem(realm));
			}
		}
		return new BaseListItemAdapter<RealmListItem>(getActivity(), listItems);
	}
}