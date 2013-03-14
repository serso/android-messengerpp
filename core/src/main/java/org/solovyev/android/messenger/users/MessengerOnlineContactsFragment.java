package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:14 PM
 */
public class MessengerOnlineContactsFragment extends AbstractMessengerContactsFragment {

    @Nonnull
    @Override
    protected AbstractAsyncLoader<UserContact, ContactListItem> createAsyncLoader(@Nonnull MessengerListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
        return new OnlineContactsAsyncLoader(getActivity(), adapter, onPostExecute, getRealmService());
    }

    @Nonnull
    protected AbstractContactsAdapter createAdapter() {
        return new OnlineContactsAdapter(getActivity(), getRealmService());
    }
}
