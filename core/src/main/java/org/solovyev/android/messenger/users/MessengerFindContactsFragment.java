package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Filter;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUiHandler;
import static org.solovyev.android.messenger.users.Users.MAX_SEARCH_CONTACTS;
import static org.solovyev.common.text.Strings.isEmpty;

public class MessengerFindContactsFragment extends AbstractMessengerContactsFragment {

	private int maxContacts = MAX_SEARCH_CONTACTS;

	private final long SEARCH_DELAY_MILLIS = 500;

	@Nonnull
	private final FindContactsRunnable runnable = new FindContactsRunnable();

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		setFilterBoxVisible();
	}

	@Nonnull
	@Override
	protected MessengerListItemAdapter<ContactListItem> createAdapter() {
		return new FoundContactsAdapter(getActivity());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiContact>> createAsyncLoader(@Nonnull MessengerListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		final CharSequence filterText = getFilterText();
		if (!isEmpty(filterText)) {
			return new FindContactsAsyncLoader(getActivity(), adapter, onPostExecute, filterText.toString(), maxContacts);
		} else {
			// in case of empty query we need to reset maxContacts
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
			final MessengerListItemAdapter adapter = getAdapter();
			if (adapter.isInitialized()) {
				createAsyncLoader(adapter).executeInParallel();
			}
		}
	}

	@Override
	public void onBottomReached() {
		super.onBottomReached();

		final CharSequence filterText = getFilterText();
		if (!isEmpty(filterText)) {
			maxContacts += MAX_SEARCH_CONTACTS;
			getUiHandler().post(runnable);
		}
	}
}
