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

package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.App;
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

import static android.telephony.TelephonyManager.PHONE_TYPE_NONE;
import static org.solovyev.android.messenger.App.newTag;

@Singleton
public final class SmsRealm extends AbstractRealm<SmsAccountConfiguration> {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	private static final String REALM_ID = "sms";
	static final String USER_ID = "self";

	public static final String INTENT_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String INTENT_SENT_PREFIX = "SMS_SENT";
	public static final String INTENT_DELIVERED_PREFIX = "SMS_DELIVERED";
	public static final String INTENT_EXTRA_SMS_ID = "sms_id";
	public static final String INTENT_EXTRA_PDUS = "pdus";
	public static final String INTENT_EXTRA_FORMAT = "format";
	public static final String TAG = newTag("SmsRealm");

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
	public SmsRealm(@Nonnull Application context) {
		super(REALM_ID, R.string.mpp_sms_name, R.drawable.mpp_sms_icon, SmsAccountConfigurationFragment.class, SmsAccountConfiguration.class, true, SmsEditUserFragment.class);
		this.context = context;
	}

	@Nonnull
	@Override
	public Account<SmsAccountConfiguration> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull SmsAccountConfiguration configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new SmsAccount(accountId, this, user, configuration, state, syncData);
	}

	@Nonnull
	@Override
	public AccountBuilder newAccountBuilder(@Nonnull SmsAccountConfiguration configuration, @Nullable Account editedAccount) {
		return new SmsAccountBuilder(this, (SmsAccount) editedAccount, configuration);
	}

	@Override
	public boolean isEnabled() {
		final TelephonyManager tm = (TelephonyManager) App.getApplication().getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getPhoneType() != PHONE_TYPE_NONE;
	}

	@Nonnull
	@Override
	public RealmIconService getRealmIconService() {
		return new SmsRealmIconService(context);
	}

	@Nullable
	@Override
	public Cipherer<SmsAccountConfiguration, SmsAccountConfiguration> getCipherer() {
		return new SmsRealmConfigurationCipherer();
	}

	/*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	@Nonnull
	static String makeSmsSentAction(@Nonnull String messageId) {
		return INTENT_SENT_PREFIX + "_" + messageId;
	}

	@Nonnull
	static String makeSmsDeliveredAction(@Nonnull String messageId) {
		return INTENT_DELIVERED_PREFIX + "_" + messageId;
	}

	private static class SmsRealmConfigurationCipherer implements Cipherer<SmsAccountConfiguration, SmsAccountConfiguration> {

		private SmsRealmConfigurationCipherer() {
		}

		@Nonnull
		public SmsAccountConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull SmsAccountConfiguration decrypted) throws CiphererException {
			return decrypted.clone();
		}

		@Nonnull
		public SmsAccountConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull SmsAccountConfiguration encrypted) throws CiphererException {
			return encrypted.clone();
		}
	}
}
