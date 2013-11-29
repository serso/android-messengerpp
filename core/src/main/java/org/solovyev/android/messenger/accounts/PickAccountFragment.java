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

import com.google.common.base.Function;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_picked;
import static org.solovyev.common.collections.Collections.isEmpty;

public class PickAccountFragment extends BaseAccountsFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "pick_account";

	private static final String ARG_ACCOUNT_IDS = "account_ids";

	public PickAccountFragment() {
		super("PickAccount", R.string.mpp_pick_account, false, true);
	}

	@Nonnull
	public static Bundle newPickAccountArguments(@Nonnull Collection<Account> accounts) {
		final Bundle arguments = new Bundle();
		arguments.putStringArray(ARG_ACCOUNT_IDS, toArray(transform(accounts, new Function<Account, String>() {
			@Override
			public String apply(Account account) {
				return account.getId();
			}
		}), String.class));
		return arguments;
	}


	@Nonnull
	@Override
	protected BaseListItemAdapter<AccountListItem> createAdapter() {
		final List<AccountListItem> listItems = new ArrayList<AccountListItem>();

		final String[] accountIds = getArguments().getStringArray(ARG_ACCOUNT_IDS);
		if (!isEmpty(accountIds)) {
			for (String accountId : accountIds) {
				listItems.add(new AccountListItem(getAccountService().getAccountById(accountId), account_picked));
			}
		}

		return new AccountsAdapter(getActivity(), listItems, false, account_picked);
	}
}
