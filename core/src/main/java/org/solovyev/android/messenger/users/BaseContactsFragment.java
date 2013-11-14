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

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseAsyncListFragment;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
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

	private ActivityMenu<Menu, MenuItem> menu;

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
			final User user = ((BaseUserFragment) fragment).getUser();
			return user != null && selectedItem.getContact().equals(user);
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
		if(this.menu == null) {
			final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();
			menuItems.add(new ToggleFilterInputMenuItem(this));
			menuItems.add(new AddContactMenuItem());
			this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contacts, menuItems, SherlockMenuHelper.getInstance());
		}
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

	private final class AddContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_add_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			getEventManager().fire(new_contact.newEvent());
		}
	}
}
