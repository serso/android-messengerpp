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

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountFragment.newAccountFragmentDef;


public final class AccountUiEventListener implements EventListener<AccountUiEvent.Typed> {

	@Nonnull
	private final BaseFragmentActivity activity;

	public AccountUiEventListener(@Nonnull BaseFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull AccountUiEvent.Typed event) {
		final Account account = event.account;

		switch (event.type) {
			case show_account:
				onShowAccountEvent(account);
				break;
			case edit_account:
				onAccountEditRequestedEvent(account);
				break;
		}
	}

	private void onAccountEditRequestedEvent(@Nonnull Account account) {
		AccountActivity.open(activity, account, true);
	}

	private void onShowAccountEvent(@Nonnull Account account) {
		MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			mpfm.clearBackStack();
			mpfm.setSecondFragment(newAccountFragmentDef(activity, account, false));
			if (activity.isTriplePane()) {
				mpfm.emptifyThirdFragment();
			}
		} else {
			mpfm.setMainFragment(newAccountFragmentDef(activity, account, true));
		}
	}
}
