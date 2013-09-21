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

	@Nonnull
	public final String getRealmId() {
		return realmId;
	}
}