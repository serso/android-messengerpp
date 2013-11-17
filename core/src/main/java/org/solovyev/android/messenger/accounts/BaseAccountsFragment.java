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
import android.support.v4.app.Fragment;
import com.google.inject.Inject;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.BaseStaticListFragment;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseAccountsFragment extends BaseStaticListFragment<AccountListItem> implements DetachableFragment {

	@Inject
	@Nonnull
	private AccountService accountService;

	@Nullable
	private JEventListener<AccountEvent> accountEventListener;

	public BaseAccountsFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, R.string.mpp_accounts, filterEnabled, selectFirstItemByDefault);
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accountEventListener = new UiAccountEventListener();
		accountService.addListener(accountEventListener);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull AccountListItem selectedItem) {
		boolean canReuse = false;
		if (fragment instanceof AccountFragment || fragment instanceof BaseAccountConfigurationFragment) {
			final Account account = ((BaseAccountFragment) fragment).getAccount();
			if (account == null) {
				canReuse = false;
			} else {
				canReuse = account.equals(selectedItem.getAccount());
			}
		}
		return canReuse;
	}

	@Override
	public void onDestroy() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
		}

		super.onDestroy();
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Nonnull
	@Override
	protected AccountsAdapter getAdapter() {
		return (AccountsAdapter) super.getAdapter();
	}

	private class UiAccountEventListener extends AbstractJEventListener<AccountEvent> {

		private UiAccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final AccountEvent accountEvent) {
			Threads2.tryRunOnUiThread(BaseAccountsFragment.this, new Runnable() {
				@Override
				public void run() {
					getAdapter().onAccountEvent(accountEvent);
				}
			});
		}
	}
}
