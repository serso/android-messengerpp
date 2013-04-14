package org.solovyev.android.messenger.realms;

import android.database.Cursor;
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

public class RealmMapper implements Converter<Cursor, Realm> {

    @Nullable
    private final SecretKey secret;

    public RealmMapper(@Nullable SecretKey secret) {
        this.secret = secret;
    }

    @Nonnull
    @Override
    public Realm convert(@Nonnull Cursor cursor) {
        final String realmId = cursor.getString(0);
        final String realmDefId = cursor.getString(1);
        final String userId = cursor.getString(2);
        final String configuration = cursor.getString(3);
        final String state = cursor.getString(4);

        try {
            final RealmDef realmDef = MessengerApplication.getServiceLocator().getRealmService().getRealmDefById(realmDefId);
            // realm is not loaded => no way we can find user in realm services
            final User user = MessengerApplication.getServiceLocator().getUserService().getUserById(EntityImpl.fromEntityId(userId), false);

            final RealmConfiguration encryptedConfiguration = new Gson().fromJson(configuration, realmDef.getConfigurationClass());

            final RealmConfiguration decryptedConfiguration;
            final Cipherer<RealmConfiguration, RealmConfiguration> cipherer = realmDef.getCipherer();
            if (secret != null && cipherer != null) {
                decryptedConfiguration = cipherer.decrypt(secret, encryptedConfiguration);
            } else {
                decryptedConfiguration = encryptedConfiguration;
            }

            return realmDef.newRealm(realmId, user, decryptedConfiguration, RealmState.valueOf(state));
        } catch (UnsupportedRealmException e) {
            throw new RealmRuntimeException(e);
        }  catch (CiphererException e) {
            throw new RealmRuntimeException(e);
        }
    }
}
