package org.solovyev.android.messenger.security;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 12:47 AM
 */
public class AuthDataImpl implements AuthData {

	@Nonnull
	private String accessToken;

	@Nonnull
	private String realmUserId;

	@Nonnull
	private String realmUserLogin;

	public AuthDataImpl() {
	}

	public void setAccessToken(@Nonnull String accessToken) {
		this.accessToken = accessToken;
	}

	public void setRealmUserId(@Nonnull String realmUserId) {
		this.realmUserId = realmUserId;
	}

	public void setRealmUserLogin(@Nonnull String realmUserLogin) {
		this.realmUserLogin = realmUserLogin;
	}

	@Override
	@Nonnull
	public String getAccessToken() {
		return accessToken;
	}


	@Override
	@Nonnull
	public String getRealmUserId() {
		return realmUserId;
	}

	@Nonnull
	@Override
	public String getRealmUserLogin() {
		return this.realmUserLogin;
	}
}
