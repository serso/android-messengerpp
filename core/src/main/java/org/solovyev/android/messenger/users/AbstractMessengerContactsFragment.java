package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:11 PM
 */
public abstract class AbstractMessengerContactsFragment extends AbstractMessengerListFragment<UserContact, ContactListItem> {

    @Nonnull
    private static String TAG = "ContactsFragment";

    public AbstractMessengerContactsFragment() {
        super(TAG);
    }

    @Override
    protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
        return null;
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }
}
