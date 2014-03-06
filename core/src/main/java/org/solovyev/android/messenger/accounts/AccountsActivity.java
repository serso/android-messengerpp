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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.RoboListeners;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountFragment.newAccountFragmentDef;

public final class AccountsActivity extends BaseFragmentActivity {

	public static void start(@Nonnull Activity activity) {
		final Intent intent = new Intent();
		intent.setClass(activity, AccountsActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			fragmentManager.setMainFragment(PrimaryFragment.accounts);
		}

		initFragments();
	}

	@Override
	protected void onAccountDisabled(@Nonnull Account account) {
		if (account.getState() == AccountState.removed && !isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof BaseAccountFragment) {
				final BaseAccountFragment af = (BaseAccountFragment) fragment;
				final Account fragmentAccount = af.getAccount();
				if (fragmentAccount != null && fragmentAccount.equals(account)) {
					tryGoBack();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		final RoboListeners listeners = getListeners();

		listeners.add(AccountUiEvent.Clicked.class, new EventListener<AccountUiEvent.Clicked>() {

			@Nonnull
			private final BaseFragmentActivity activity = AccountsActivity.this;

			@Override
			public void onEvent(AccountUiEvent.Clicked event) {
				if (isDualPane()) {
					fragmentManager.clearBackStack();
					fragmentManager.setSecondFragment(newAccountFragmentDef(activity, event.account, false));
					if (isTriplePane()) {
						fragmentManager.emptifyThirdFragment();
					}
				} else {
					fragmentManager.setMainFragment(newAccountFragmentDef(activity, event.account, true));
				}
			}
		});
	}
}
