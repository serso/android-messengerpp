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
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener.newAccountRemoverListener;

public class AccountFragment extends BaseAccountFragment<Account<?>> {

	@Nonnull
	public static final String FRAGMENT_TAG = "account";

	private final AccountButtons buttons = new AccountButtons(this);

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

		updateHeaderView(root, account);
		updateStatusView(root, account);

		buttons.onViewCreated(root, savedInstanceState);

		onAccountStateChanged(account, root);
	}

	private void updateStatusView(@Nonnull View root, @Nonnull Account<?> account) {
		final View status = root.findViewById(R.id.mpp_account_status);

		final TextView statusLabel = (TextView) status.findViewById(R.id.mpp_property_label);
		statusLabel.setText(R.string.mpp_status);

		final TextView statusValue = (TextView) status.findViewById(R.id.mpp_property_value);
		statusValue.setText(account.getUser().getDisplayName());
	}

	private void updateHeaderView(@Nonnull View root, @Nonnull Account<?> account) {
		final Realm realm = account.getRealm();

		final View header = root.findViewById(R.id.mpp_account_header);

		final ImageView accountIcon = (ImageView) header.findViewById(R.id.mpp_property_icon);
		accountIcon.setImageDrawable(getResources().getDrawable(realm.getIconResId()));
		accountIcon.setVisibility(View.VISIBLE);

		final TextView realmName = (TextView) header.findViewById(R.id.mpp_property_label);
		realmName.setText(realm.getNameResId());

		final TextView userName = (TextView) header.findViewById(R.id.mpp_property_value);
		userName.setText(account.getUser().getDisplayName());
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
			updateHeaderView(root, account);
			buttons.updateAccountViews(account, root);
		}
	}
}
