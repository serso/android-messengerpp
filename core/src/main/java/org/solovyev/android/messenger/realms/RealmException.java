package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmException extends Exception {

	@Nonnull
	private final String realmId;

	public RealmException(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	public RealmException(@Nonnull String realmId, @Nonnull Throwable throwable) {
		super(unwrap(throwable));
		this.realmId = realmId;
	}

	public RealmException(@Nonnull RealmRuntimeException exception) {
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
