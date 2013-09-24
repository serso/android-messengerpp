package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerListItemAdapter;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:14 PM
 */
public class MessengerOnlineContactsFragment extends AbstractMessengerContactsFragment {

	@Nonnull
	@Override
	protected AbstractAsyncLoader<UiContact, ContactListItem> createAsyncLoader(@Nonnull MessengerListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new OnlineContactsAsyncLoader(getActivity(), adapter, onPostExecute, getAccountService());
	}

	@Nonnull
	protected AbstractContactsAdapter createAdapter() {
		return new OnlineContactsAdapter(getActivity(), getAccountService());
	}
}
