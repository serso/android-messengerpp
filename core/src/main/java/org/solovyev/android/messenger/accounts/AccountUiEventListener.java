package org.solovyev.android.messenger.accounts;

import android.os.Bundle;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.users.Users;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountFragment.newAccountFragmentDef;


/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:50 PM
 */
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
			case account_view_requested:
				onAccountViewRequestedEvent(account);
				break;
			case account_view_cancelled:
				onAccountViewCancelledEvent(account);
				break;
			case account_edit_requested:
				onAccountEditRequestedEvent(account);
				break;
			case account_edit_finished:
				onAccountEditFinishedEvent(event);
				break;
			case account_picked:
				Users.tryShowCreateUserFragment(account, activity);
				break;
		}
	}

	private void onAccountViewCancelledEvent(@Nonnull Account account) {
		activity.getSupportFragmentManager().popBackStack();
	}

	private void onAccountEditRequestedEvent(@Nonnull Account account) {
		final Bundle fragmentArgs = BaseAccountConfigurationFragment.newEditAccountArguments(account);
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();
		fm.setSecondOrMainFragment(account.getRealm().getConfigurationFragmentClass(), fragmentArgs, BaseAccountConfigurationFragment.FRAGMENT_TAG);
	}

	private void onAccountViewRequestedEvent(@Nonnull Account account) {
		MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			mpfm.setSecondFragment(newAccountFragmentDef(activity, account));
			if (activity.isTriplePane()) {
				mpfm.emptifyThirdFragment();
			}
		} else {
			mpfm.setMainFragment(newAccountFragmentDef(activity, account));
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
				activity.getMultiPaneFragmentManager().clearBackStack();
				break;
		}
	}
}
