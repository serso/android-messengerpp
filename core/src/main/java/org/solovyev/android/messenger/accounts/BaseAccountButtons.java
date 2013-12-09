package org.solovyev.android.messenger.accounts;

import android.content.DialogInterface;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ConfirmationDialogBuilder;

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
		final F f = getFragment();
		final ConfirmationDialogBuilder builder = ConfirmationDialogBuilder.newInstance(f.getFragmentActivity(), "account-removal-confirmation", R.string.mpp_account_removal_confirmation);
		builder.setPositiveHandler(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				f.getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
			}
		});
		builder.build().show();
	}

	@Nonnull
	protected A getAccount() {
		return getFragment().getAccount();
	}
}
