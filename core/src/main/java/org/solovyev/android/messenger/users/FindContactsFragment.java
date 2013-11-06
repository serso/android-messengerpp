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

package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Filter;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUiHandler;
import static org.solovyev.android.messenger.users.Users.MAX_SEARCH_CONTACTS;
import static org.solovyev.common.text.Strings.isEmpty;

public class FindContactsFragment extends BaseContactsFragment {

	private int maxContacts = MAX_SEARCH_CONTACTS;

	private static final long SEARCH_DELAY_MILLIS = 500;

	@Nonnull
	private final FindContactsRunnable runnable = new FindContactsRunnable();

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		setFilterBoxVisible();
	}

	@Nonnull
	@Override
	protected BaseListItemAdapter<ContactListItem> createAdapter() {
		return new FoundContactsAdapter(getActivity(), isRecentContacts());
	}

	private boolean isRecentContacts() {
		return isEmpty(getFilterText());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiContact>> createAsyncLoader(@Nonnull BaseListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		final CharSequence filterText = getFilterText();
		if (!isEmpty(filterText)) {
			((FoundContactsAdapter) adapter).setRecentContacts(false);
			return new FindContactsAsyncLoader(getActivity(), adapter, onPostExecute, filterText.toString(), maxContacts);
		} else {
			// in case of empty query we need to reset maxContacts
			((FoundContactsAdapter) adapter).setRecentContacts(true);
			maxContacts = MAX_SEARCH_CONTACTS;
			return new RecentContactsAsyncLoader(getActivity(), adapter, onPostExecute, maxContacts);
		}
	}

	@Override
	public void filter(@Nullable CharSequence filterText) {
		if (getAdapter().isInitialized()) {
			final Handler handler = getUiHandler();
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, SEARCH_DELAY_MILLIS);
		}
	}

	@Override
	public void filter(@Nullable CharSequence filterText, @Nullable Filter.FilterListener filterListener) {
		if (filterListener != null) {
			filterListener.onFilterComplete(0);
		}
	}

	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	private class FindContactsRunnable implements Runnable {

		public FindContactsRunnable() {
		}

		@Override
		public void run() {
			final BaseListItemAdapter adapter = getAdapter();
			if (adapter.isInitialized()) {
				adapter.unselect();
				createAsyncLoader(adapter).executeInParallel();
			}
		}
	}

	@Override
	public void onBottomReached() {
		super.onBottomReached();

		if (!isRecentContacts()) {
			maxContacts += MAX_SEARCH_CONTACTS;
			getUiHandler().post(runnable);
		}
	}
}
