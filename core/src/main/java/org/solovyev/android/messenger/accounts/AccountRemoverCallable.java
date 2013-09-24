package org.solovyev.android.messenger.accounts;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:02 PM
 */
final class AccountRemoverCallable implements Callable<Account> {

	@Nonnull
	static final String TASK_NAME = "realm-remove";

	@Nonnull
	private final Account account;

	AccountRemoverCallable(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public Account call() {
		App.getAccountService().removeAccount(account.getId());

		return account;
	}
}
