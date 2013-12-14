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

import android.util.Log;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.AHttpClient;
import org.solovyev.android.messenger.accounts.AbstractAccountBuilder;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.vk.auth.JsonAuthResult;
import org.solovyev.android.messenger.realms.vk.auth.VkOauthHttpTransaction;
import org.solovyev.android.messenger.realms.vk.users.VkUsersGetHttpTransaction;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static org.solovyev.android.http.HttpTransactions.execute;
import static org.solovyev.android.http.HttpTransactions.newHttpClient;
import static org.solovyev.android.messenger.accounts.Accounts.newNeverSyncedData;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;

public class VkAccountBuilder extends AbstractAccountBuilder<VkAccount, VkAccountConfiguration> {

	protected VkAccountBuilder(@Nonnull Realm realm, @Nullable VkAccount editedAccount, @Nonnull VkAccountConfiguration configuration) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		final String userId = getConfiguration().getUserId();
		final MutableUser defaultUser = newEmptyUser(newEntity(accountId, userId));

		MutableUser result;
		try {
			final List<User> users = execute(VkUsersGetHttpTransaction.newInstance(new VkAccount(accountId, getRealm(), defaultUser, getConfiguration(), AccountState.removed, newNeverSyncedData()), userId, null));
			if (users.isEmpty()) {
				result = defaultUser;
			} else {
				final User user = users.get(0);
				result = newUser(user.getEntity(), user.getProperties());
			}
		} catch (Exception e) {
			Log.e(VkRealm.TAG, e.getMessage(), e);
			result = defaultUser;
		}

		return result;
	}

	@Nonnull
	@Override
	protected VkAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new VkAccount(id, getRealm(), user, getConfiguration(), state, syncData);
	}

	@Override
	public void connect() throws ConnectionException {
	}

	@Override
	public void disconnect() throws ConnectionException {
	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
		final VkAccountConfiguration configuration = getConfiguration();

		try {
			final AHttpClient httpClient = newHttpClient();
			final JsonAuthResult authResult = httpClient.execute(new VkOauthHttpTransaction(configuration.getLogin(), configuration.getPassword()));
			if (authResult != null) {
				final String accessToken = authResult.getAccessToken();
				final String userId = authResult.getUserId();
				if (accessToken != null && userId != null) {
					configuration.setAccessParameters(accessToken, userId);
					return;
				}
			}
		} catch (AccountRuntimeException e) {
			throw new InvalidCredentialsException(e);
		} catch (IOException e) {
			throw new InvalidCredentialsException(e);
		}

		throw new InvalidCredentialsException();
	}

}
