package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public final class RealmRuntimeException extends RuntimeException {

	@Nonnull
	private final String realmId;

	public RealmRuntimeException(@Nonnull AccountException e) {
		this(e.getRealmId(), e);
	}

	public RealmRuntimeException(@Nonnull String realmId, Throwable throwable) {
		super(throwable);
		this.realmId = realmId;
	}

	@Nonnull
	public String getRealmId() {
		return realmId;
	}
}
