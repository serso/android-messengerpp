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
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.sync.SyncAllAsyncTask;
import org.solovyev.android.messenger.view.PropertyView;
import org.solovyev.android.messenger.view.SwitchView;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener.newAccountRemoverListener;
import static org.solovyev.android.messenger.view.PropertyView.newPropertyView;

public class AccountFragment extends BaseAccountFragment<Account<?>> {

	@Nonnull
	public static final String FRAGMENT_TAG = "account";

	private final AccountButtons buttons = new AccountButtons(this);

	private PropertyView headerView;
	private PropertyView statusView;
	private SwitchView statusSwitch;
	private PropertyView syncView;
	private ImageView syncButton;

	public AccountFragment() {
		super(R.layout.mpp_fragment_account, true);
	}

	@Nonnull
	public static MultiPaneFragmentDef newAccountFragmentDef(@Nonnull Context context, @Nonnull Account account, boolean addToBackStack) {
		final Bundle args = newAccountArguments(account);
		final JPredicate<Fragment> reuseCondition = AccountFragmentReuseCondition.forAccount(account);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, AccountFragment.class, context, args, reuseCondition);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final Account<?> account = getAccount();
		final Context context = getThemeContext();

		headerView = newPropertyView(R.id.mpp_account_header, root);

		statusView = newPropertyView(R.id.mpp_account_status, root);

		statusSwitch = new SwitchView(context);
		statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Account<?> account = getAccount();
				if (account.isEnabled() != isChecked) {
					buttons.changeState();
				}
			}
		});

		syncButton = new ImageView(context);
		syncButton.setImageDrawable(getResources().getDrawable(R.drawable.mpp_ab_refresh_light));
		syncView = newPropertyView(R.id.mpp_account_sync, root);
		syncView.getView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SyncAllAsyncTask.newForAccount(getActivity(), getSyncService(), account).executeInParallel((Void) null);
			}
		});

		updateView(account);

		buttons.onViewCreated(root, savedInstanceState);

		onAccountStateChanged(account, root);
	}

	private void updateView(@Nonnull Account<?> account) {
		final Realm realm = account.getRealm();

		statusSwitch.setChecked(account.isEnabled());

		headerView.setLabel(realm.getNameResId())
				.setValue(account.getUser().getDisplayName())
				.setIcon(realm.getIconResId());

		statusView.setLabel(R.string.mpp_status)
				.setValue(account.isEnabled() ? R.string.mpp_enabled : R.string.mpp_disabled)
				.setWidget(statusSwitch.getView());

		final DateTime lastSyncDate = account.getSyncData().getLastContactsSyncDate();
		syncView.setLabel(R.string.mpp_sync)
				.setValue(lastSyncDate == null ? null : lastSyncDate.toString(DateTimeFormat.mediumDateTime()))
				.setWidget(syncButton)
				.getView().setEnabled(account.isEnabled());
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return getAccount().getDisplayName(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();

		getTaskListeners().addTaskListener(AccountChangeStateCallable.TASK_NAME, AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
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
		if (root != null) {
			updateView(account);
			buttons.updateAccountViews(account, root);
		}
	}
}
