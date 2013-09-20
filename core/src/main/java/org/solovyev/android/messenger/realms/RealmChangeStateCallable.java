package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:12 PM
 */
final class RealmChangeStateCallable implements Callable<Account> {

	@Nonnull
	static final String TASK_NAME = "realm-change-state";

	@Nonnull
	private final Account account;

	RealmChangeStateCallable(@Nonnull Account account) {
		this.account = account;
	}

	@Override
	public Account call() throws Exception {
		final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();
		return realmService.changeRealmState(account, account.isEnabled() ? AccountState.disabled_by_user : AccountState.enabled);
	}
}
