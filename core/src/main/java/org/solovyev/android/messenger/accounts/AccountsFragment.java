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

package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import static org.solovyev.android.messenger.UiEventType.show_realms;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_view_requested;

public class AccountsFragment extends BaseAccountsFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "accounts";

	private ActivityMenu<Menu, MenuItem> menu;

	public AccountsFragment() {
		super("Accounts", false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup root = super.onCreateView(inflater, container, savedInstanceState);

		addFooterButton(root, R.string.mpp_account_add, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getEventManager().fire(show_realms.newEvent());
			}
		});

		return root;
	}

	@Nonnull
	@Override
	protected BaseListItemAdapter<AccountListItem> createAdapter() {
		final List<AccountListItem> listItems = new ArrayList<AccountListItem>();
		for (Account account : getAccountService().getAccounts()) {
			if (account.getState() != AccountState.removed) {
				listItems.add(new AccountListItem(account, account_view_requested));
			}
		}
		return new AccountsAdapter(getActivity(), listItems, true, account_view_requested);
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
		if (this.menu == null) {
			this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_accounts, AccountsMenu.class, SherlockMenuHelper.getInstance());
		}

		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item) || super.onOptionsItemSelected(item);
	}

	private static enum AccountsMenu implements IdentifiableMenuItem<MenuItem> {
		account_add(R.id.mpp_menu_account_add) {
			@Override
			public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(show_realms.newEvent());
			}
		};

		private final int menuItemId;

		AccountsMenu(int menuItemId) {
			this.menuItemId = menuItemId;
		}

		@Nonnull
		@Override
		public Integer getItemId() {
			return this.menuItemId;
		}
	}
}
