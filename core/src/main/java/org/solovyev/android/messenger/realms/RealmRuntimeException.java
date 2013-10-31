package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmRuntimeException extends RuntimeException {

	@Nonnull
	private final String realmId;

	public RealmRuntimeException(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	public RealmRuntimeException(@Nonnull String realmId, @Nonnull Throwable throwable) {
		super(throwable);
		this.realmId = realmId;
	}

	@Nonnull
	public final String getRealmId() {
		return realmId;
	}
}
