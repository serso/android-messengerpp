package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

public class OnlineContactsFragment extends BaseContactsFragment {

	@Nonnull
	@Override
	protected AbstractAsyncLoader<UiContact, ContactListItem> createAsyncLoader(@Nonnull BaseListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new OnlineContactsAsyncLoader(getActivity(), adapter, onPostExecute, getAccountService());
	}

	@Nonnull
	protected AbstractContactsAdapter createAdapter() {
		return new OnlineContactsAdapter(getActivity());
	}

	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new ContactsSyncRefreshListener();
	}
}
