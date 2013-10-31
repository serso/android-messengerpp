package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

public class AccountRuntimeException extends RuntimeException {

	@Nonnull
	private final String accountId;

	public AccountRuntimeException(@Nonnull AccountException e) {
		this(e.getAccountId(), e);
	}

	public AccountRuntimeException(@Nonnull String accountId, Throwable throwable) {
		super(throwable);
		this.accountId = accountId;
	}

	public AccountRuntimeException(@Nonnull String accountId) {
		this.accountId = accountId;
	}

	@Nonnull
	public String getAccountId() {
		return accountId;
	}
}
