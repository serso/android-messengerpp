package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.UiEventType.new_contact;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:11 PM
 */
public abstract class AbstractMessengerContactsFragment extends AbstractMessengerListFragment<UiContact, ContactListItem> implements DetachableFragment {

	@Nonnull
	private static String TAG = newTag("ContactsFragment");

	public AbstractMessengerContactsFragment() {
		super(TAG, true, true);
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup root = super.onCreateView(inflater, container, savedInstanceState);

		if (getAccountService().canCreateUsers()) {
			addFooterButton(root, R.string.mpp_contact_add, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getEventManager().fire(new_contact.newEvent());
				}
			});
		}

		return root;
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
