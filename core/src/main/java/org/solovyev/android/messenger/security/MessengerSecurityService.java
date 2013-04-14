package org.solovyev.android.messenger.security;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerPreferences;
import org.solovyev.android.security.Security;
import org.solovyev.android.security.base64.ABase64StringDecoder;
import org.solovyev.android.security.base64.ABase64StringEncoder;
import org.solovyev.common.security.CiphererException;
import org.solovyev.common.security.SaltGenerator;
import org.solovyev.common.security.SecurityService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.UUID;

/**
 * User: serso
 * Date: 4/14/13
 * Time: 3:25 PM
 */
@Singleton
public final class MessengerSecurityService {

    @Nonnull
    private static final String TAG = MessengerSecurityService.class.getSimpleName();

    @Nonnull
    private final SecurityService<byte[], byte[], byte[]> securityService;

    @Nonnull
    private final SecurityService<String, String, String> stringSecurityService;

    @Nonnull
    private final Application context;

    @Inject
    public MessengerSecurityService(@Nonnull Application context) {
        this.context = context;
        this.securityService = Security.newAndroidAesByteSecurityService();
        this.stringSecurityService = Security.newAndroidStringSecurityService(securityService);
    }

    @Nonnull
    public SecurityService<byte[], byte[], byte[]> getSecurityService() {
        return securityService;
    }

    @Nonnull
    public SecurityService<String, String, String> getStringSecurityService() {
        return stringSecurityService;
    }

    @Nullable
    public synchronized SecretKey getSecretKey() {
        try {
            final SaltGenerator saltGenerator = securityService.getSaltGenerator();

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String uuid = MessengerPreferences.Security.uuid.getPreference(preferences);
            String salt = MessengerPreferences.Security.salt.getPreference(preferences);
            if (uuid == null || salt == null) {
                uuid = UUID.randomUUID().toString();
                MessengerPreferences.Security.uuid.putPreference(preferences, uuid);
                salt = ABase64StringEncoder.getInstance().convert(saltGenerator.generateSalt());
                MessengerPreferences.Security.salt.putPreference(preferences, salt);
            }

            return securityService.getSecretKeyProvider().getSecretKey(uuid + Build.DEVICE + "Messenger++", ABase64StringDecoder.getInstance().convert(salt));
        } catch (CiphererException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }
}
