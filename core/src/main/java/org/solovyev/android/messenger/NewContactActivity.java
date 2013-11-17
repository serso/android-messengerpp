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

package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.realms.Realm;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.Collection;

import static org.solovyev.android.messenger.accounts.PickAccountFragment.newPickAccountArguments;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.pick_account;
import static org.solovyev.android.messenger.users.BaseEditUserFragment.newCreateUserFragmentDef;

public final class NewContactActivity extends BaseFragmentActivity {


	public static void start(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, NewContactActivity.class);
		activity.startActivity(result);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			final Collection<Account> accounts = getAccountService().getAccountsCreatingUsers();
			getMultiPaneFragmentManager().setMainFragment(pick_account, newPickAccountArguments(accounts));
		}

		final RoboListeners listeners = getListeners();
		listeners.add(AccountUiEvent.class, new AccountUiEventListener());

		initFragments();
	}

	private final class AccountUiEventListener implements EventListener<AccountUiEvent> {

		@Override
		public void onEvent(@Nonnull AccountUiEvent event) {
			final Account account = event.getAccount();

			switch (event.getType()) {
				case account_picked:
					tryShowCreateUserFragment(account);
					if (isTriplePane()) {
						getMultiPaneFragmentManager().emptifyThirdFragment();
					}
					break;
			}
		}
	}

	public boolean tryShowCreateUserFragment(@Nonnull Account account) {
		final Realm realm = account.getRealm();
		if (realm.canCreateUsers()) {
			final MessengerMultiPaneFragmentManager mpfm = getMultiPaneFragmentManager();
			if (isDualPane()) {
				mpfm.setSecondFragment(newCreateUserFragmentDef(this, account, false));
			} else {
				mpfm.setMainFragment(newCreateUserFragmentDef(this, account, true));
			}
			return true;
		}

		return false;
	}

}
