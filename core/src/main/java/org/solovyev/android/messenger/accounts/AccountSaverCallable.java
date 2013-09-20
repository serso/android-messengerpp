package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

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
		return MessengerApplication.getServiceLocator().getAccountService().saveAccount(accountBuilder);
	}
}