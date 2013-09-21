package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

public final class AccountConnectionException extends AccountException {

	public AccountConnectionException(@Nonnull String accountId) {
		super(accountId);
	}

	public AccountConnectionException(@Nonnull String accountId, @Nonnull Throwable throwable) {
		super(accountId, throwable);
	}
}
