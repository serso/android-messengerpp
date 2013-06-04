package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmException extends Exception {

	@Nonnull
	private final String realmId;

	public RealmException(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	public RealmException(@Nonnull String realmId, @Nonnull Throwable throwable) {
		super(throwable);
		this.realmId = realmId;
	}

	public RealmException(@Nonnull RealmRuntimeException exception) {
		super(unwrap(exception));
		this.realmId = exception.getRealmId();
	}

	@Nonnull
	private static Throwable unwrap(@Nonnull RealmRuntimeException exception) {
		final Throwable cause = exception.getCause();
		return cause != null ? cause : exception;
	}

	@Nonnull
	public final String getRealmId() {
		return realmId;
	}
}
