package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.accounts.AccountException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:12 AM
 */
public final class UnsupportedAccountException extends AccountException {

	public UnsupportedAccountException(@Nonnull String realmId) {
		super(realmId);
	}
}
