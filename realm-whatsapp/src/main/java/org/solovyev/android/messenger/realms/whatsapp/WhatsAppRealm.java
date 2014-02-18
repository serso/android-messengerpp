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

import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import static org.solovyev.android.messenger.App.newTag;

@Singleton
public final class WhatsAppRealm extends AbstractRealm<WhatsAppAccountConfiguration> {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	private static final String REALM_ID = "whatsapp";
	public static final String TAG = newTag("WhatsAppRealm");


	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final Context context;

	@Inject
	public WhatsAppRealm(@Nonnull Application context) {
		super(REALM_ID, R.string.mpp_whatsapp_name, R.drawable.mpp_whatsapp_icon, WhatsAppAccountConfigurationFragment.class, WhatsAppAccountConfiguration.class, false, null, true);
		this.context = context;
	}

	@Nonnull
	@Override
	public Account<WhatsAppAccountConfiguration> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull WhatsAppAccountConfiguration configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new WhatsAppAccount(accountId, this, user, configuration, state, syncData);
	}

	@Nonnull
	@Override
	public AccountBuilder newAccountBuilder(@Nonnull WhatsAppAccountConfiguration configuration, @Nullable Account editedAccount) {
		return new WhatsAppAccountBuilder(this, (WhatsAppAccount) editedAccount, configuration);
	}

	@Nonnull
	@Override
	public RealmIconService getRealmIconService() {
		return new WhatsappRealmIconService(context);
	}

	@Nullable
	@Override
	public Cipherer<WhatsAppAccountConfiguration, WhatsAppAccountConfiguration> getCipherer() {
		return new WhatsappRealmConfigurationCipherer();
	}

	/*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class WhatsappRealmConfigurationCipherer implements Cipherer<WhatsAppAccountConfiguration, WhatsAppAccountConfiguration> {

		private WhatsappRealmConfigurationCipherer() {
		}

		@Nonnull
		public WhatsAppAccountConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull WhatsAppAccountConfiguration decrypted) throws CiphererException {
			return decrypted.clone();
		}

		@Nonnull
		public WhatsAppAccountConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull WhatsAppAccountConfiguration encrypted) throws CiphererException {
			return encrypted.clone();
		}
	}
}
