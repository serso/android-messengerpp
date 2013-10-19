package org.solovyev.android.messenger.accounts;

import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.AbstractFragmentReuseCondition;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;

/**
 * Fragment will be reused if it's instance of {@link org.solovyev.android.messenger.accounts.AccountFragment} and
 * contains same realm as one passed in constructor
 */
class AccountFragmentReuseCondition extends AbstractFragmentReuseCondition<AccountFragment> {

	@Nonnull
	private final Account account;

	AccountFragmentReuseCondition(@Nonnull Account account) {
		super(AccountFragment.class);
		this.account = account;
	}

	@Nonnull
	public static JPredicate<Fragment> forAccount(@Nonnull Account account) {
		return new AccountFragmentReuseCondition(account);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull AccountFragment fragment) {
		return account.equals(fragment.getAccount());
	}
}
