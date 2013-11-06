/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
