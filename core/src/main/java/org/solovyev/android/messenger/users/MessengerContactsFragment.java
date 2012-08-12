package org.solovyev.android.messenger.users;

import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 4:09 PM
 */
public class MessengerContactsFragment extends AbstractMessengerContactsFragment {

    @NotNull
    protected AbstractAsyncLoader<User> createAsyncLoader(@NotNull AbstractMessengerListItemAdapter adapter, @NotNull Runnable onPostExecute) {
        return new ContactsAsyncLoader(getUser(), getActivity(), adapter, onPostExecute);
    }

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getSyncService().sync(SyncTask.user_contacts, getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            completeRefresh();
                        }
                    });
                    Toast.makeText(getActivity(), "User contacts sync started!", Toast.LENGTH_SHORT).show();
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(getActivity());
                }
            }
        };
    }

    @NotNull
    protected AbstractContactsAdapter createAdapter() {
        return new ContactsAdapter(getActivity(), getUser());
    }
}
