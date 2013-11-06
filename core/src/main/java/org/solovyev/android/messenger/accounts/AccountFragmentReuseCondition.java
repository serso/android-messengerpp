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
