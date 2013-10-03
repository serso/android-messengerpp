package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 4:09 PM
 */
public final class ContactsFragment extends AbstractContactsFragment {

	@Nonnull
	private static final String MODE = "mode";

	@Nonnull
	private ContactsDisplayMode mode = Users.DEFAULT_CONTACTS_MODE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			final Object mode = savedInstanceState.getSerializable(MODE);
			if (mode instanceof ContactsDisplayMode) {
				changeMode((ContactsDisplayMode) mode);
			}
		}
	}

	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new ContactsSyncRefreshListener();
	}

	@Nonnull
	protected MessengerAsyncTask<Void, Void, List<UiContact>> createAsyncLoader(@Nonnull MessengerListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new ContactsAsyncLoader(getActivity(), adapter, onPostExecute);
	}

	@Nonnull
	protected AbstractContactsAdapter createAdapter() {
		return new ContactsAdapter(getActivity());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(MODE, mode);
	}

	private void changeMode(@Nonnull ContactsDisplayMode newMode) {
		mode = newMode;
		((AbstractContactsAdapter) getAdapter()).setMode(newMode);
	}

	@Override
	public void onResume() {
		super.onResume();

		((AbstractContactsAdapter) getAdapter()).setMode(mode);
	}

	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	private ActivityMenu<Menu, MenuItem> menu;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
		final MenuItem contactsMenuItem = menu.findItem(R.id.mpp_menu_toggle_contacts);
		contactsMenuItem.setIcon(mode.getActionBarIconResId());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		menuItems.add(new ToggleContactsMenuItem());
		menuItems.add(new ToggleFilterInputMenuItem(this));

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contacts, menuItems, SherlockMenuHelper.getInstance());
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	private class ToggleContactsMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_toggle_contacts;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			final ContactsDisplayMode newMode = mode == ContactsDisplayMode.only_online_contacts ? ContactsDisplayMode.all_contacts : ContactsDisplayMode.only_online_contacts;
			changeMode(newMode);
			menuItem.setIcon(newMode.getActionBarIconResId());
		}
	}
}
