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

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.tasks.AccountSaverCallable;
import org.solovyev.android.messenger.core.R;

import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.back;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.messenger.accounts.tasks.AccountSaverListener.newAccountSaverListener;
import static org.solovyev.android.messenger.realms.RealmUiEventType.realm_edit_finished;

public class AccountEditButtons<A extends Account<?>> extends BaseAccountButtons<A, BaseAccountConfigurationFragment<A>> {

	public AccountEditButtons(@Nonnull BaseAccountConfigurationFragment<A> fragment) {
		super(fragment);
	}

	protected final void onSaveButtonPressed() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		final AccountConfiguration configuration = fragment.validateData();
		if (configuration != null) {
			final AccountBuilder accountBuilder = fragment.getRealm().newAccountBuilder(configuration, fragment.getEditedAccount());
			saveAccount(accountBuilder);
		}
	}

	protected void onBackButtonPressed() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		A editedAccount = fragment.getEditedAccount();
		if (editedAccount != null) {
			fragment.getEventManager().fire(account_edit_finished.newEvent(editedAccount, back));
		} else {
			fragment.getEventManager().fire(realm_edit_finished.newEvent(fragment.getRealm()));
		}
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		return !getFragment().isNewAccount();
	}

	@Override
	protected boolean isBackButtonVisible() {
		return true;
	}

	public void saveAccount(@Nonnull AccountBuilder accountBuilder) {
		getFragment().getTaskListeners().run(AccountSaverCallable.TASK_NAME, new AccountSaverCallable(accountBuilder), newAccountSaverListener(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}
}
