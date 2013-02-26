package org.solovyev.android.messenger.users;

import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:14 PM
 */
public class MessengerOnlineContactsFragment extends AbstractMessengerContactsFragment {

    @NotNull
    @Override
    protected AbstractAsyncLoader<UserContact, ContactListItem> createAsyncLoader(@NotNull AbstractMessengerListItemAdapter<ContactListItem> adapter, @NotNull Runnable onPostExecute) {
        return new OnlineContactsAsyncLoader(getActivity(), adapter, onPostExecute, getRealmService());
    }

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    AbstractMessengerApplication.getServiceLocator().getSyncService().sync(SyncTask.check_online_user_contacts, getActivity(), new Runnable() {
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

    @NotNull
    protected AbstractContactsAdapter createAdapter() {
        return new OnlineContactsAdapter(getActivity());
    }
}
