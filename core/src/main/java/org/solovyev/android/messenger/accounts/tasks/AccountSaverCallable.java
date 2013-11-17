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

package org.solovyev.android.messenger.accounts.tasks;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountAlreadyExistsException;
import org.solovyev.android.messenger.accounts.AccountBuilder;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

public class AccountSaverCallable implements Callable<Account> {

	@Nonnull
	public static final String TASK_NAME = "account-save";

	@Nonnull
	private final AccountBuilder accountBuilder;

	public AccountSaverCallable(@Nonnull AccountBuilder accountBuilder) {
		this.accountBuilder = accountBuilder;
	}

	@Override
	public Account call() throws InvalidCredentialsException, AccountAlreadyExistsException {
		return App.getAccountService().saveAccount(accountBuilder);
	}

}