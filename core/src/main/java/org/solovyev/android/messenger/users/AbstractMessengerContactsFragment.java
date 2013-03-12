package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.fragments.DetachableFragment;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:11 PM
 */
public abstract class AbstractMessengerContactsFragment extends AbstractMessengerListFragment<UserContact, ContactListItem> implements DetachableFragment {

    @Nonnull
    private static String TAG = "ContactsFragment";

    public AbstractMessengerContactsFragment() {
        super(TAG, true, true);
    }

    @Override
    protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
        return null;
    }

}
