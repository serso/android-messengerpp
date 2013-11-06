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

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAccountBuilder<A extends Account<C>, C extends AccountConfiguration> implements AccountBuilder<A> {

	@Nonnull
	private Realm realm;

	@Nonnull
	private C configuration;

	@Nullable
	private A editedAccount;

	protected AbstractAccountBuilder(@Nonnull Realm realm,
									 @Nonnull C configuration,
									 @Nullable A editedAccount) {
		this.realm = realm;
		this.editedAccount = editedAccount;
		this.configuration = configuration;
	}

	@Nonnull
	public C getConfiguration() {
		return configuration;
	}

	@Nonnull
	@Override
	public final A build(@Nonnull Data data) {
		final String accountId = data.getAccountId();

		final MutableUser user = getAccountUser(accountId);

		// account user should always be online
		user.setOnline(true);

		return newAccount(accountId, user, AccountState.enabled);
	}

	@Nonnull
	protected abstract MutableUser getAccountUser(@Nonnull String accountId);

	@Nonnull
	public Realm getRealm() {
		return realm;
	}

	@Nullable
	@Override
	public A getEditedAccount() {
		return this.editedAccount;
	}

	@Nonnull
	protected abstract A newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state);
}
