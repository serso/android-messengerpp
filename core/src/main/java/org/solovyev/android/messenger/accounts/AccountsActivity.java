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
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.realms.RealmUiEvent;
import org.solovyev.android.messenger.realms.RealmUiEventListener;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.fragments.PrimaryFragment.realms;

public final class AccountsActivity extends BaseFragmentActivity {

	@Nonnull
	private static final String EXTRA_NEW_ACCOUNTS = "new-accounts";

	public static void start(@Nonnull Activity activity) {
		final Intent intent = new Intent();
		intent.setClass(activity, AccountsActivity.class);
		activity.startActivity(intent);
	}

	public static void startForNewAccounts(@Nonnull Activity activity) {
		final Intent intent = new Intent();
		intent.putExtra(EXTRA_NEW_ACCOUNTS, true);
		intent.setClass(activity, AccountsActivity.class);
		activity.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			getMultiPaneFragmentManager().setMainFragment(PrimaryFragment.accounts);

			// show realms if needed
			if (getIntent().getBooleanExtra(EXTRA_NEW_ACCOUNTS, false)) {
				getMultiPaneFragmentManager().setMainFragment(realms);
			}
		}

		initFragments();
	}

	@Override
	protected void onResume() {
		super.onResume();

		final RoboListeners listeners = getListeners();

		listeners.add(UiEvent.class, new UiEventListener(this));
		listeners.add(AccountUiEvent.Typed.class, new AccountUiEventListener(this));
		listeners.add(AccountUiEvent.EditFinished.class, new EventListener<AccountUiEvent.EditFinished>() {
			@Override
			public void onEvent(AccountUiEvent.EditFinished event) {
				switch (event.state) {
					case back:
						fragmentManager.goBack();
						break;
					case removed:
						fragmentManager.clearBackStack();
						break;
					case status_changed:
						// do nothing as we can change state only from account info fragment and that is OK
						break;
					case saved:
						fragmentManager.goBack();
						break;
				}
			}
		});
		listeners.add(RealmUiEvent.class, new RealmUiEventListener(this));
	}
}
