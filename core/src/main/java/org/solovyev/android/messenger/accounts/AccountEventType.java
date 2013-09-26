package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/9/13
 * Time: 2:45 PM
 */
public enum AccountEventType {

	/**
	 * Fired when account is created
	 */
	created,

	/**
	 * Fired when account is changed
	 */
	changed,

	/**
	 * Fired when only account configuration has been changed
	 */
	configuration_changed,

	/**
	 * Fired when account state is changed
	 */
	state_changed,

	/**
	 * Fired when account connection should be stopped for account
	 */
	stop,

	/**
	 * Fired when account connection should be started for account
	 */
	start;

	@Nonnull
	AccountEvent newEvent(@Nonnull Account account, @Nullable Object data) {
		assert data == null;
		return new AccountEvent(account, this, null);
	}
}
