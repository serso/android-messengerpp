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

package org.solovyev.android.messenger.realms.whatsapp;

import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.accounts.AbstractAccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class WhatsAppAccountBuilder extends AbstractAccountBuilder<WhatsAppAccount, WhatsAppAccountConfiguration> {

	WhatsAppAccountBuilder(@Nonnull Realm realm, @Nullable WhatsAppAccount editedAccount, @Nonnull WhatsAppAccountConfiguration configuration) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Nonnull
	@Override
	protected WhatsAppAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new WhatsAppAccount(id, getRealm(), user, getConfiguration(), state, syncData);
	}

	@Override
	public void connect() throws ConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Override
	public void disconnect() throws ConnectionException {
		throw new UnsupportedOperationException("Whats up?");
	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
		throw new UnsupportedOperationException("Whats up?");
	}
}
