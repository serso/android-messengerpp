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

package org.solovyev.android.messenger.accounts;

import android.database.Cursor;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Converter;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import com.google.gson.Gson;

import static org.solovyev.android.messenger.App.getRealmService;
import static org.solovyev.android.messenger.App.getUserService;

public class AccountMapper<C extends AccountConfiguration> implements Converter<Cursor, Account<C>> {

	@Nullable
	private final SecretKey secret;

	public AccountMapper(@Nullable SecretKey secret) {
		this.secret = secret;
	}

	@Nonnull
	@Override
	public Account<C> convert(@Nonnull Cursor cursor) {
		final String accountId = cursor.getString(0);
		final String realmId = cursor.getString(1);
		final String userId = cursor.getString(2);
		final String configuration = cursor.getString(3);
		final String state = cursor.getString(4);

		final Realm<C> realm = (Realm<C>) getRealmService().getRealmById(realmId);
		// realm is not loaded => no way we can find user in realm services
		final User user = getUserService().getUserById(Entities.newEntityFromEntityId(userId), false);

		final C encryptedConfiguration = new Gson().fromJson(configuration, realm.getConfigurationClass());

		final C decryptedConfiguration = decryptConfiguration(realm, encryptedConfiguration);

		return realm.newAccount(accountId, user, decryptedConfiguration, AccountState.valueOf(state));
	}

	@Nonnull
	private C decryptConfiguration(@Nonnull Realm<C> realm, @Nonnull C encryptedConfiguration) {
		try {
			final C decryptedConfiguration;
			final Cipherer<C, C> cipherer = realm.getCipherer();
			if (secret != null && cipherer != null) {
				decryptedConfiguration = cipherer.decrypt(secret, encryptedConfiguration);
			} else {
				decryptedConfiguration = encryptedConfiguration;
			}
			return decryptedConfiguration;
		} catch (CiphererException e) {
			Log.e("Realm", e.getMessage(), e);
			// user will see an error notification later when realm will try to connect to remote server
			return encryptedConfiguration;
		}
	}
}
