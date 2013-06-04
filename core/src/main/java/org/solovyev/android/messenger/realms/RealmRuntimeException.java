package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmRuntimeException extends RuntimeException {

	@Nonnull
	private final String realmId;

	public RealmRuntimeException(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	public RealmRuntimeException(@Nonnull RealmException e) {
		super(e);
		this.realmId = e.getRealmId();
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
