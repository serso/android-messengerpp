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
import android.widget.Button;
import android.widget.ImageView;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.common.JPredicate;

import com.google.inject.Inject;

import static org.solovyev.android.messenger.accounts.AccountUiEventType.edit_account;

public class AccountFragment extends BaseAccountFragment<Account<?>> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	public static final String FRAGMENT_TAG = "account";

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private EventManager eventManager;

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
		final Realm realm = account.getRealm();

		final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
		realmIconImageView.setImageDrawable(getResources().getDrawable(realm.getIconResId()));

		buttons.onViewCreated(root, savedInstanceState);

		onAccountStateChanged(account, root);
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return getAccount().getDisplayName(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();

		getTaskListeners().addTaskListener(AccountChangeStateCallable.TASK_NAME, AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	void changeState() {
		getTaskListeners().run(AccountChangeStateCallable.TASK_NAME, new AccountChangeStateCallable(getAccount()), AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	void editAccount() {
		eventManager.fire(edit_account.newEvent(getAccount()));
	}

	void removeAccount() {
		getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
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
		return BaseAccountFragment.newAccountArguments(account);
	}

	protected void onAccountStateChanged(@Nonnull Account<?> account, @Nullable View root) {
		if (root != null) {
			buttons.updateAccountViews(account, root);
		}
	}
}
