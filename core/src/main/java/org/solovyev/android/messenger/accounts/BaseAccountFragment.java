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

import android.os.Bundle;
import android.view.View;
import com.google.inject.Inject;
import org.solovyev.android.messenger.BaseFragment;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.UiThreadEventListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.accounts.Accounts.getAccountIdFromArguments;

public abstract class BaseAccountFragment<A extends Account<?>> extends BaseFragment {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	/*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private EventManager eventManager;
	
	/*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private A account;

	@Nullable
	private JEventListener<AccountEvent> accountEventListener;

	protected BaseAccountFragment(int layoutResId) {
		super(layoutResId);
	}

	public BaseFragmentActivity getFragmentActivity() {
		return (BaseFragmentActivity) super.getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String accountId = getAccountIdFromArguments(arguments);
			if (accountId != null) {
				account = (A) accountService.getAccountById(accountId);

				accountEventListener = UiThreadEventListener.onUiThread(this, new AccountEventListener());
				accountService.addListener(accountEventListener);
			}
		}
	}

	protected void onAccountStateChanged(@Nonnull A account, @Nullable View root) {
	}

	protected void onAccountChanged(@Nonnull A account, @Nullable View root) {
	}

	@Override
	public void onDestroy() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
			accountEventListener = null;
		}

		super.onDestroy();
	}

	@Nonnull
	protected AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	protected EventManager getEventManager() {
		return eventManager;
	}

	public A getAccount() {
		return account;
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	private final class AccountEventListener extends AbstractJEventListener<AccountEvent> {

		protected AccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account eventAccount = event.getAccount();
			switch (event.getType()) {
				case configuration_changed:
				case sync_data_changed:
				case changed:
					if (eventAccount.equals(account)) {
						account = (A) eventAccount;
						onAccountChanged(account, getView());
					}
					break;
				case state_changed:
					if (eventAccount.equals(account)) {
						account = (A) eventAccount;
						onAccountStateChanged(account, getView());
					}
					break;
			}
		}
	}
}
