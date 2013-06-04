package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmConnectionException extends RealmException {

	public RealmConnectionException(@Nonnull String realmId) {
		super(realmId);
	}

	public RealmConnectionException(@Nonnull String realmId, @Nonnull Throwable throwable) {
		super(realmId, throwable);
	}

	public RealmConnectionException(@Nonnull RealmRuntimeException exception) {
		super(exception);
	}
}
