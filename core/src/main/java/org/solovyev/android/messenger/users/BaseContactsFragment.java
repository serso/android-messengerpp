/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.BaseAsyncListFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.UiEventType.new_contact;

public abstract class BaseContactsFragment extends BaseAsyncListFragment<UiContact, ContactListItem> implements DetachableFragment {

	@Nonnull
	private static String TAG = newTag("ContactsFragment");

	public BaseContactsFragment() {
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

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull ContactListItem selectedItem) {
		if (fragment instanceof BaseUserFragment) {
			return selectedItem.getContact().equals(((BaseUserFragment) fragment).getUser());
		}
		return false;
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
				showToast(R.string.mpp_updating_contacts);
			} catch (TaskIsAlreadyRunningException e) {
				completeRefresh();
				e.showMessage();
			}
		}
	}
}
