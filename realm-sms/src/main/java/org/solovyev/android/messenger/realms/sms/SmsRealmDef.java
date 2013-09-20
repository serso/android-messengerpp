package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.accounts.AbstractRealmDef;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountBuilder;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:45 PM
 */
@Singleton
public final class SmsRealmDef extends AbstractRealmDef<SmsAccountConfiguration> {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	private static final String REALM_ID = "sms";
	static final String USER_ID = "self";

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
	public SmsRealmDef(@Nonnull Application context) {
		super(REALM_ID, R.string.mpp_sms_realm_name, R.drawable.mpp_sms_icon, SmsAccountConfigurationFragment.class, SmsAccountConfiguration.class, false);
		this.context = context;
	}

	@Nonnull
	@Override
	public Account<SmsAccountConfiguration> newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull SmsAccountConfiguration configuration, @Nonnull AccountState state) {
		return new SmsAccount(realmId, this, user, configuration, state);
	}

	@Nonnull
	@Override
	public AccountBuilder newRealmBuilder(@Nonnull SmsAccountConfiguration configuration, @Nullable Account editedAccount) {
		return new SmsAccountBuilder(this, editedAccount, configuration);
	}

	@Nonnull
	@Override
	public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
		// todo serso: implement
		return Collections.emptyList();
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
