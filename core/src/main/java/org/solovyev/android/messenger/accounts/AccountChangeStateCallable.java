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

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:12 PM
 */
final class AccountChangeStateCallable implements Callable<Account> {

	@Nonnull
	static final String TASK_NAME = "account-change-state";

	@Nonnull
	private final Account account;

	AccountChangeStateCallable(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public Account call() throws Exception {
		final AccountService accountService = App.getAccountService();
		return accountService.changeAccountState(account, account.isEnabled() ? AccountState.disabled_by_user : AccountState.enabled);
	}
}
