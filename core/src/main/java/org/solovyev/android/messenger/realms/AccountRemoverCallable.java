package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

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
		MessengerApplication.getServiceLocator().getAccountService().removeAccount(account.getId());

		return account;
	}
}
