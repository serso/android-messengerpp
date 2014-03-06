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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CompoundButton;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.EmptyFutureCallback;
import org.solovyev.android.messenger.MessengerTheme;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.sync.SyncAllAsyncTask;
import org.solovyev.android.messenger.view.FragmentMenu;
import org.solovyev.android.messenger.view.PropertyView;
import org.solovyev.android.messenger.view.SwitchView;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ConfirmationDialogBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.view.PropertyView.newPropertyView;

public class AccountFragment extends BaseAccountFragment<Account<?>> {

	@Nonnull
	public static final String FRAGMENT_TAG = "account";

	private PropertyView headerView;
	private PropertyView statusView;
	private SwitchView statusSwitch;
	private PropertyView syncView;
	private View accountOnline;

	@Nonnull
	private final FragmentMenu menu = new FragmentMenu(this, new MenuBuilder());

	public AccountFragment() {
		super(R.layout.mpp_fragment_account);
	}

	@Nonnull
	public static MultiPaneFragmentDef newAccountFragmentDef(@Nonnull Context context, @Nonnull Account account, boolean addToBackStack) {
		final Bundle args = newAccountArguments(account);
		final JPredicate<Fragment> reuseCondition = AccountFragmentReuseCondition.forAccount(account);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, AccountFragment.class, context, args, reuseCondition);
	}

	@Nonnull
	public static MultiPaneFragmentDef newAccountFragmentDef(@Nonnull Context context, @Nonnull Bundle arguments, boolean addToBackStack) {
		final JPredicate<Fragment> reuseCondition = new AccountFragmentReuseCondition(Accounts.getAccountIdFromArguments(arguments), AccountFragment.class);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, AccountFragment.class, context, arguments, reuseCondition);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final Account<?> account = getAccount();
		final Context context = getThemeContext();
		final MessengerTheme.Icons icons = getIcons();

		final Realm realm = account.getRealm();

		accountOnline = ViewFromLayoutBuilder.newInstance(R.layout.mpp_online).build(context);

		headerView = newPropertyView(R.id.mpp_account_header, root);
		headerView.setLabel(realm.getNameResId())
				.setIcon(realm.getIconResId())
				.setWidget(accountOnline);

		statusView = newPropertyView(R.id.mpp_account_status, root);

		newPropertyView(R.id.mpp_account_edit, root)
				.setLabel(R.string.mpp_edit)
				.setRightIcon(icons.edit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						editAccount();
					}
				});

		newPropertyView(R.id.mpp_account_remove, root)
				.setLabel(R.string.mpp_remove)
				.setRightIcon(icons.remove)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						tryRemoveAccount();

					}
				});

		statusSwitch = new SwitchView(context);
		statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Account<?> account = getAccount();
				if (account.isEnabled() != isChecked) {
					changeState();
				}
			}
		});

		syncView = newPropertyView(R.id.mpp_account_sync, root)
				.setRightIcon(R.drawable.mpp_ab_refresh_light);
		syncView.getView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SyncAllAsyncTask.newForAccount(getActivity(), getSyncService(), account).executeInParallel((Void) null);
			}
		});

		updateView(account);

		onAccountStateChanged(account, root);
	}

	private void tryRemoveAccount() {
		final ConfirmationDialogBuilder builder = ConfirmationDialogBuilder.newInstance(getSherlockActivity(), "account-removal-confirmation", R.string.mpp_account_removal_confirmation);
		builder.setPositiveHandler(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), null, getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
			}
		});
		builder.build().show();
	}

	private void updateView(@Nonnull Account<?> account) {
		statusSwitch.setChecked(account.isEnabled());

		headerView.setValue(account.getUser().getDisplayName());
		if (account.isEnabled()) {
			accountOnline.setVisibility(account.isOnline() ? VISIBLE : GONE);
		} else {
			accountOnline.setVisibility(GONE);
		}

		statusView.setLabel(R.string.mpp_status)
				.setValue(account.isEnabled() ? R.string.mpp_enabled : R.string.mpp_disabled)
				.setWidget(statusSwitch.getView());

		final DateTime lastSyncDate = account.getSyncData().getLastContactsSyncDate();
		syncView.setLabel(R.string.mpp_sync)
				.setValue(lastSyncDate == null ? null : lastSyncDate.toString(DateTimeFormat.mediumDateTime()))
				.getView().setEnabled(account.isEnabled());
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_account_info);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateView(getAccount());

		getTaskListeners().addTaskListener(AccountChangeStateCallable.TASK_NAME, EmptyFutureCallback.instance, getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, EmptyFutureCallback.instance, getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	void editAccount() {
		getEventManager().fire(new AccountUiEvent.Edit(getAccount()));
	}

	void changeState() {
		getTaskListeners().run(AccountChangeStateCallable.TASK_NAME, new AccountChangeStateCallable(getAccount()), null, getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	@Nonnull
	public static Bundle newAccountArguments(@Nonnull Account account) {
		return Accounts.newAccountArguments(account);
	}

	protected void onAccountStateChanged(@Nonnull Account<?> account, @Nullable View root) {
		super.onAccountStateChanged(account, root);
		if (root != null) {
			updateView(account);
		}
	}

	@Override
	protected void onAccountChanged(@Nonnull Account<?> account, @Nullable View root) {
		super.onAccountChanged(account, root);
		if (root != null) {
			updateView(account);
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
		this.menu.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(item);
	}

	private class EditAccountMenuItem implements IdentifiableMenuItem<MenuItem> {
		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_edit_account;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			editAccount();
		}
	}

	private class RemoveAccountMenuItem implements IdentifiableMenuItem<MenuItem> {
		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_remove_account;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			tryRemoveAccount();
		}
	}

	private class MenuBuilder implements Builder<ActivityMenu<Menu, MenuItem>> {
		@Nonnull
		@Override
		public ActivityMenu<Menu, MenuItem> build() {
			final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>(2);
			menuItems.add(new RemoveAccountMenuItem());
			menuItems.add(new EditAccountMenuItem());
			return ListActivityMenu.fromResource(R.menu.mpp_menu_account, menuItems, SherlockMenuHelper.getInstance());
		}
	}
}
