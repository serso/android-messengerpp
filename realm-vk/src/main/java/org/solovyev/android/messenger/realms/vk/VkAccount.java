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

import android.content.Context;
import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.vk.chats.VkAccountChatService;
import org.solovyev.android.messenger.realms.vk.users.VkAccountUserService;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public final class VkAccount extends AbstractAccount<VkAccountConfiguration> {

	public VkAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull VkAccountConfiguration configuration, @Nonnull AccountState state) {
		super(id, realm, user, configuration, state);
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new VkLongPollAccountConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		final StringBuilder sb = new StringBuilder();

		sb.append(context.getText(getRealm().getNameResId()));

		return sb.toString();
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new VkAccountUserService(this);
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new VkAccountChatService(this);
	}
}
