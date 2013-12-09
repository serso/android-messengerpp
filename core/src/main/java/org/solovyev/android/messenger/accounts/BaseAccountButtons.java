package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.core.R;

import static org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener.newAccountRemoverListener;

public abstract class BaseAccountButtons<A extends Account<?>, F extends BaseAccountFragment<A>> extends EditButtons<F>{

	public BaseAccountButtons(@Nonnull F fragment) {
		super(fragment);
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		return true;
	}

	@Override
	protected void onRemoveButtonPressed() {
		final F fragment = getFragment();
		fragment.getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	@Nonnull
	protected A getAccount() {
		return getFragment().getAccount();
	}
}
