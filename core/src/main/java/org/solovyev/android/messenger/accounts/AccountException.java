package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.realms.RealmRuntimeException;

import javax.annotation.Nonnull;

public class AccountException extends Exception {

	@Nonnull
	private final String realmId;

	public AccountException(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	public AccountException(@Nonnull String realmId, @Nonnull Throwable throwable) {
		super(unwrap(throwable));
		this.realmId = realmId;
	}

	public AccountException(@Nonnull RealmRuntimeException exception) {
		super(unwrap(exception));
		this.realmId = exception.getRealmId();
	}

	@Nonnull
	private static Throwable unwrap(@Nonnull Throwable exception) {
		if (exception instanceof RealmRuntimeException) {
			final Throwable cause = exception.getCause();
			return cause != null ? cause : exception;
		} else {
			return exception;
		}
	}

	@Nonnull
	public final String getRealmId() {
		return realmId;
	}
}
