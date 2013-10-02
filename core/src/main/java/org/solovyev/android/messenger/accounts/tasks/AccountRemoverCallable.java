package org.solovyev.android.messenger.accounts.tasks;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:02 PM
 */
public final class AccountRemoverCallable implements Callable<Account> {

	@Nonnull
	public static final String TASK_NAME = "realm-remove";

	@Nonnull
	private final Account account;

	public AccountRemoverCallable(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public Account call() {
		App.getAccountService().removeAccount(account.getId());

		return account;
	}
}
