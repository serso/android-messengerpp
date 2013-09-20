package org.solovyev.android.messenger.realms;

import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.common.BuilderWithData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RealmBuilder extends BuilderWithData<Realm, RealmBuilder.Data> {

	@Nonnull
	RealmDef getRealmDef();

	@Nullable
	Realm getEditedRealm();

	void connect() throws ConnectionException;

	void disconnect() throws ConnectionException;

	void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

	@Nonnull
	AccountConfiguration getConfiguration();

	public static final class Data {

		@Nonnull
		private final String realmId;

		public Data(@Nonnull String realmId) {
			this.realmId = realmId;
		}

		@Nonnull
		public String getRealmId() {
			return realmId;
		}
	}

	public static class ConnectionException extends Exception {

		public ConnectionException() {
		}

		public ConnectionException(Throwable throwable) {
			super(throwable);
		}
	}
}
