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

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountFragment.newAccountFragmentDef;
import static org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment.newEditAccountConfigurationFragmentDef;


public final class AccountUiEventListener implements EventListener<AccountUiEvent> {

	@Nonnull
	private final BaseFragmentActivity activity;

	public AccountUiEventListener(@Nonnull BaseFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull AccountUiEvent event) {
		final Account account = event.getAccount();

		switch (event.getType()) {
			case show_account:
				onAccountViewRequestedEvent(account);
				break;
			case edit_account:
				onAccountEditRequestedEvent(account);
				break;
			case account_edit_finished:
				onAccountEditFinishedEvent(event);
				break;
		}
	}

	private void onAccountEditRequestedEvent(@Nonnull Account account) {
		final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
		final MultiPaneFragmentDef fragmentDef = newEditAccountConfigurationFragmentDef(activity, account, true);
		if (activity.isDualPane()) {
			mpfm.setSecondFragment(fragmentDef);
		} else {
			mpfm.setMainFragment(fragmentDef);
		}
	}

	private void onAccountViewRequestedEvent(@Nonnull Account account) {
		MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			mpfm.setSecondFragment(newAccountFragmentDef(activity, account, false));
			if (activity.isTriplePane()) {
				mpfm.emptifyThirdFragment();
			}
		} else {
			mpfm.setMainFragment(newAccountFragmentDef(activity, account, true));
		}
	}

	private void onAccountEditFinishedEvent(@Nonnull AccountUiEvent event) {
		final AccountUiEventType.FinishedState state = (AccountUiEventType.FinishedState) event.getData();
		assert state != null;
		switch (state) {
			case back:
				activity.getMultiPaneFragmentManager().goBack();
				break;
			case removed:
				activity.getMultiPaneFragmentManager().clearBackStack();
				break;
			case status_changed:
				// do nothing as we can change state only from realm info fragment and that is OK
				break;
			case saved:
				activity.getMultiPaneFragmentManager().goBack();
				break;
		}
	}
}
