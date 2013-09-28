package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.View;
import android.widget.Filter;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUiHandler;

public class MessengerFindContactsFragment extends AbstractMessengerContactsFragment {

	private final long SEARCH_DELAY_MILLIS = 300;

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
		return new FindContactsAsyncLoader(getActivity(), adapter, onPostExecute, filterText == null ? null : filterText.toString());
	}

	@Override
	public void filter(@Nullable CharSequence filterText) {
		if (getAdapter().isInitialized()) {
			getUiHandler().removeCallbacks(runnable);
			getUiHandler().postDelayed(runnable, SEARCH_DELAY_MILLIS);
		}
	}

	@Override
	public void filter(@Nullable CharSequence filterText, @Nullable Filter.FilterListener filterListener) {
		if (filterListener != null) {
			filterListener.onFilterComplete(0);
		}
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
}
