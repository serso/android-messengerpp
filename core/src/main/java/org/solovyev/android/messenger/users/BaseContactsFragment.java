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

import android.content.Context;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseAsyncListFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.UiEventType.new_contact;

public abstract class BaseContactsFragment extends BaseAsyncListFragment<UiContact, ContactListItem> implements DetachableFragment {

	@Nonnull
	private static String TAG = newTag("ContactsFragment");

	@Nonnull
	private final ActivityMenu<Menu, MenuItem> menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contacts, ContactsMenu.class, SherlockMenuHelper.getInstance());

	public BaseContactsFragment() {
		super(TAG, true, true);

		setHasOptionsMenu(true);
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
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

	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			final MenuItem item = menu.getItem(i);
			if (item.getItemId() == R.id.mpp_menu_add_contact) {
				item.setVisible(getAccountService().canCreateUsers());
			}
		}
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item) || super.onOptionsItemSelected(item);
	}

	private static enum ContactsMenu implements IdentifiableMenuItem<MenuItem> {
		add_contact(R.id.mpp_menu_add_contact) {
			@Override
			public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
				App.getEventManager(context).fire(new_contact.newEvent());
			}
		};

		private final int menuItemId;

		ContactsMenu(int menuItemId) {
			this.menuItemId = menuItemId;
		}

		@Nonnull
		@Override
		public Integer getItemId() {
			return this.menuItemId;
		}
	}
}
