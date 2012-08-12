package org.solovyev.android.messenger.users;

import android.os.AsyncTask;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:11 PM
 */
public abstract class AbstractMessengerContactsFragment extends AbstractMessengerListFragment<User> {

    @NotNull
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

    @Override
    protected void updateRightPane() {
        int selectedItemPosition = getSelectedItemPosition();

        final AbstractMessengerListItemAdapter adapter = getAdapter();
        if (selectedItemPosition >= 0 && selectedItemPosition < adapter.getCount()) {
            final ContactListItem contactListItem = (ContactListItem) adapter.getItem(selectedItemPosition);

            new AsyncTask<Void, Void, Chat>() {

                @Override
                protected Chat doInBackground(Void... params) {
                    return getUserService().getPrivateChat(contactListItem.getUser().getId(), contactListItem.getContact().getId(), getActivity());
                }

                @Override
                protected void onPostExecute(@NotNull Chat chat) {
                    super.onPostExecute(chat);

                }

            }.execute(null, null);
        }
    }


}
