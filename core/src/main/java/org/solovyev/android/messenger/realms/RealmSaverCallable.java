package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:05 PM
 */
class RealmSaverCallable implements Callable<Account> {

	@Nonnull
	static final String TASK_NAME = "realm-save";

	@Nonnull
	private final RealmBuilder realmBuilder;

	RealmSaverCallable(@Nonnull RealmBuilder realmBuilder) {
		this.realmBuilder = realmBuilder;
	}

	@Override
	public Account call() throws InvalidCredentialsException, RealmAlreadyExistsException {
		return MessengerApplication.getServiceLocator().getAccountService().saveAccount(realmBuilder);
	}
}