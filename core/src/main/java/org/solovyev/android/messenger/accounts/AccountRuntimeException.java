package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.accounts.AccountException;

import javax.annotation.Nonnull;

public final class AccountRuntimeException extends RuntimeException {

	@Nonnull
	private final String realmId;

	public AccountRuntimeException(@Nonnull AccountException e) {
		this(e.getRealmId(), e);
	}

	public AccountRuntimeException(@Nonnull String realmId, Throwable throwable) {
		super(throwable);
		this.realmId = realmId;
	}

	@Nonnull
	public String getRealmId() {
		return realmId;
	}
}
