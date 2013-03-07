package org.solovyev.android.messenger.users;

import android.widget.Toast;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

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

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    MessengerApplication.getServiceLocator().getSyncService().sync(SyncTask.check_online_user_contacts, new Runnable() {
                        @Override
                        public void run() {
                            completeRefresh();
                        }
                    });
                    Toast.makeText(getActivity(), "Online statuses check started!", Toast.LENGTH_SHORT).show();
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(getActivity());
                }
            }
        };
    }

    @Nonnull
    protected AbstractContactsAdapter createAdapter() {
        return new OnlineContactsAdapter(getActivity());
    }
}
