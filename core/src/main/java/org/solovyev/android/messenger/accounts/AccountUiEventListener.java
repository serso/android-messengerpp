package org.solovyev.android.messenger.accounts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.AbstractFragmentReuseCondition;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.BaseAccountFragment.ARG_ACCOUNT_ID;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:50 PM
 */
public final class AccountUiEventListener implements EventListener<AccountUiEvent> {

	@Nonnull
	private static final String TAG = AccountUiEventListener.class.getSimpleName();

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
		}
	}

	private void onAccountViewCancelledEvent(@Nonnull Account account) {
		activity.getSupportFragmentManager().popBackStack();
	}

	private void onAccountEditRequestedEvent(@Nonnull Account account) {
		final Bundle fragmentArgs = new Bundle();
		fragmentArgs.putString(ARG_ACCOUNT_ID, account.getId());
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();
		fm.setSecondOrMainFragment(account.getRealm().getConfigurationFragmentClass(), null, BaseAccountConfigurationFragment.FRAGMENT_TAG);
	}

	private void onAccountViewRequestedEvent(@Nonnull Account account) {
		if (activity.isDualPane()) {
			showRealmFragment(account, false);
			if (activity.isTriplePane()) {
				activity.getMultiPaneFragmentManager().emptifyThirdFragment();
			}
		} else {
			showRealmFragment(account, true);
		}
	}

	private void showRealmFragment(@Nonnull Account account, boolean firstPane) {
		final Bundle fragmentArgs = new Bundle();
		fragmentArgs.putString(AccountFragment.ARGS_ACCOUNT_ID, account.getId());
		if (firstPane) {
			activity.getMultiPaneFragmentManager().setMainFragment(AccountFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(account), AccountFragment.FRAGMENT_TAG, true);
		} else {
			activity.getMultiPaneFragmentManager().setSecondFragment(AccountFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(account), AccountFragment.FRAGMENT_TAG, false);
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
				activity.getMultiPaneFragmentManager().goBackTillStart();
				if (activity.isDualPane()) {
					activity.getMultiPaneFragmentManager().emptifySecondFragment();
				}
				break;
			case status_changed:
				// do nothing as we can change state only from realm info fragment and that is OK
				break;
			case saved:
				activity.getMultiPaneFragmentManager().goBackTillStart();
				break;
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	/**
	 * Fragment will be reused if it's instance of {@link AccountFragment} and
	 * contains same realm as one passed in constructor
	 */
	private static class RealmFragmentReuseCondition extends AbstractFragmentReuseCondition<AccountFragment> {

		@Nonnull
		private final Account account;

		private RealmFragmentReuseCondition(@Nonnull Account account) {
			super(AccountFragment.class);
			this.account = account;
		}

		@Nonnull
		public static JPredicate<Fragment> forRealm(@Nonnull Account account) {
			return new RealmFragmentReuseCondition(account);
		}

		@Override
		protected boolean canReuseFragment(@Nonnull AccountFragment fragment) {
			return account.equals(fragment.getAccount());
		}
	}
}
