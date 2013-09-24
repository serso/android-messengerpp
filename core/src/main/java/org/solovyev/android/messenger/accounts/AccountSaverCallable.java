package org.solovyev.android.messenger.accounts;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:05 PM
 */
class AccountSaverCallable implements Callable<Account> {

	@Nonnull
	static final String TASK_NAME = "realm-save";

	@Nonnull
	private final AccountBuilder accountBuilder;

	AccountSaverCallable(@Nonnull AccountBuilder accountBuilder) {
		this.accountBuilder = accountBuilder;
	}

	@Override
	public Account call() throws InvalidCredentialsException, AccountAlreadyExistsException {
		return App.getAccountService().saveAccount(accountBuilder);
	}
}