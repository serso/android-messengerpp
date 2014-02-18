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

import android.content.Context;
import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.newTag;

final class WhatsAppAccount extends AbstractAccount<WhatsAppAccountConfiguration> {

	static final String TAG = newTag(WhatsAppAccount.class.getSimpleName());

	public WhatsAppAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull WhatsAppAccountConfiguration configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		super(id, realm, user, configuration, state, syncData);
	}

	@Nonnull
	@Override
	public WhatsAppRealm getRealm() {
		return (WhatsAppRealm) super.getRealm();
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new WhatsAppAccountConnection(this, context);
	}

	@Nullable
	@Override
	protected synchronized WhatsAppAccountConnection getConnection() {
		return (WhatsAppAccountConnection) super.getConnection();
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealm().getNameResId());
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new WhatsAppAccountUserService(this);
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new WhatsAppAccountChatService(this);
	}
}
