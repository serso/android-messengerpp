package org.solovyev.android.messenger.accounts;

import android.os.Bundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.messenger.AbstractListFragment;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import com.google.inject.Inject;

public abstract class AbstractAccountsFragment extends AbstractListFragment<Account, AccountListItem> implements DetachableFragment {

	@Inject
	@Nonnull
	private AccountService accountService;

	@Nullable
	private JEventListener<AccountEvent> accountEventListener;

	public AbstractAccountsFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		accountEventListener = new UiAccountEventListener();
		accountService.addListener(accountEventListener);
	}

	@Override
	public void onDestroyView() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
		}
		super.onDestroyView();
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
			Threads2.tryRunOnUiThread(AbstractAccountsFragment.this, new Runnable() {
				@Override
				public void run() {
					getAdapter().onAccountEvent(accountEvent);
				}
			});
		}
	}
}
