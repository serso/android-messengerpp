package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import android.util.Log;
import com.google.gson.Gson;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Converter;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

public class RealmMapper<C extends AccountConfiguration> implements Converter<Cursor, Account<C>> {

	@Nullable
	private final SecretKey secret;

	public RealmMapper(@Nullable SecretKey secret) {
		this.secret = secret;
	}

	@Nonnull
	@Override
	public Account<C> convert(@Nonnull Cursor cursor) {
		final String realmId = cursor.getString(0);
		final String realmDefId = cursor.getString(1);
		final String userId = cursor.getString(2);
		final String configuration = cursor.getString(3);
		final String state = cursor.getString(4);

		try {
			final RealmDef<C> realmDef = (RealmDef<C>) MessengerApplication.getServiceLocator().getAccountService().getRealmDefById(realmDefId);
			// realm is not loaded => no way we can find user in realm services
			final User user = MessengerApplication.getServiceLocator().getUserService().getUserById(EntityImpl.fromEntityId(userId), false);

			final C encryptedConfiguration = new Gson().fromJson(configuration, realmDef.getConfigurationClass());

			final C decryptedConfiguration = decryptConfiguration(realmDef, encryptedConfiguration);

			return realmDef.newRealm(realmId, user, decryptedConfiguration, AccountState.valueOf(state));
		} catch (UnsupportedRealmException e) {
			throw new RealmRuntimeException(realmId, e);
		}
	}

	@Nonnull
	private C decryptConfiguration(@Nonnull RealmDef<C> realmDef, @Nonnull C encryptedConfiguration) {
		try {
			final C decryptedConfiguration;
			final Cipherer<C, C> cipherer = realmDef.getCipherer();
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
