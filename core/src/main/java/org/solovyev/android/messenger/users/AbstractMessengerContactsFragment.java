package org.solovyev.android.messenger.users;

import android.widget.Toast;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:11 PM
 */
public abstract class AbstractMessengerContactsFragment extends AbstractMessengerListFragment<UiContact, ContactListItem> implements DetachableFragment {

	@Nonnull
	private static String TAG = "ContactsFragment";

	public AbstractMessengerContactsFragment() {
		super(TAG, true, true);
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	protected class ContactsSyncRefreshListener extends AbstractOnRefreshListener {
		@Override
		public void onRefresh() {
			try {
				getSyncService().sync(SyncTask.user_contacts_statuses, new Runnable() {
					@Override
					public void run() {
						completeRefresh();
					}
				});
				Toast.makeText(getActivity(), "User contacts presence sync started!", Toast.LENGTH_SHORT).show();
			} catch (TaskIsAlreadyRunningException e) {
				completeRefresh();
				e.showMessage(getActivity());
			}
		}
	}
}
