package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

public class AccountException extends Exception {

	@Nonnull
	private final String accountId;

	public AccountException(@Nonnull String accountId) {
		this.accountId = accountId;
	}

	public AccountException(@Nonnull String accountId, @Nonnull Throwable throwable) {
		super(unwrap(throwable));
		this.accountId = accountId;
	}

	public AccountException(@Nonnull AccountRuntimeException exception) {
		super(unwrap(exception));
		this.accountId = exception.getAccountId();
	}

	@Nonnull
	private static Throwable unwrap(@Nonnull Throwable exception) {
		if (exception instanceof AccountRuntimeException) {
			final Throwable cause = exception.getCause();
			return cause != null ? cause : exception;
		} else {
			return exception;
		}
	}

	@Nonnull
	public final String getAccountId() {
		return accountId;
	}
}
