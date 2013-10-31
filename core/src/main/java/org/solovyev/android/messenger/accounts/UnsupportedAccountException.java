package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

public final class UnsupportedAccountException extends AccountRuntimeException {

	public UnsupportedAccountException(@Nonnull String accountId) {
		super(accountId);
	}

	public UnsupportedAccountException(@Nonnull String accountId, Throwable throwable) {
		super(accountId, throwable);
	}
}
