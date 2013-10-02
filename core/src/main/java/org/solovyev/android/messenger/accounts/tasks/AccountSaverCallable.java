package org.solovyev.android.messenger.accounts.tasks;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountAlreadyExistsException;
import org.solovyev.android.messenger.accounts.AccountBuilder;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:05 PM
 */
public class AccountSaverCallable implements Callable<Account> {

	@Nonnull
	public static final String TASK_NAME = "realm-save";

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