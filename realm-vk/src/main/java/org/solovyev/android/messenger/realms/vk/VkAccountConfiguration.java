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

package org.solovyev.android.messenger.realms.vk;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class VkAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	private String login;

	@Nonnull
	private transient String password = "";

	@Nonnull
	private String accessToken;

	@Nonnull
	private String userId;

	// for json
	public VkAccountConfiguration() {
	}

	public VkAccountConfiguration(@Nonnull String login, @Nonnull String password) {
		this.login = login;
		this.password = password;
	}

	@Nonnull
	public String getAccessToken() {
		return accessToken;
	}

	@Nonnull
	@Override
	public VkAccountConfiguration clone() {
		return (VkAccountConfiguration) super.clone();
	}

	@Nonnull
	public String getLogin() {
		return login;
	}

	@Nonnull
	public String getPassword() {
		return password;
	}

	public void setAccessParameters(@Nonnull String accessToken, @Nonnull String userId) {
		this.accessToken = accessToken;
		this.userId = userId;
		// we obtained access token => password is not needed anymore
		this.password = "";
	}


	@Nonnull
	public String getUserId() {
		return userId;
	}

	@Override
	public boolean isSameAccount(AccountConfiguration c) {
		if (c == this) return true;
		if (!(c instanceof VkAccountConfiguration)) return false;

		final VkAccountConfiguration that = (VkAccountConfiguration) c;

		if (!login.equals(that.login)) return false;

		return true;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		boolean same = isSameAccount(c);
		if (same) {
			final VkAccountConfiguration that = (VkAccountConfiguration) c;
			same = this.password.equals(that.password);
		}
		return same;
	}

	@Override
	public boolean isSame(AccountConfiguration c) {
		boolean same = isSameCredentials(c);
		if (same) {
			final VkAccountConfiguration that = (VkAccountConfiguration) c;
			same = this.userId.equals(that.userId) && this.accessToken.equals(that.accessToken);
		}
		return same;
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
		if (oldConfiguration instanceof VkAccountConfiguration) {
			applySystemData((VkAccountConfiguration) oldConfiguration);
		}
	}

	public void applySystemData(VkAccountConfiguration oldConfiguration) {
		this.accessToken = oldConfiguration.accessToken;
		this.userId = oldConfiguration.userId;
	}
}
