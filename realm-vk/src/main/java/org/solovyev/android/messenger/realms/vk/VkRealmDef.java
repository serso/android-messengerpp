package org.solovyev.android.messenger.realms.vk;

import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.icons.HttpRealmIconService;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.android.security.Security;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 8/12/12
* Time: 10:34 PM
*/
@Singleton
public class VkRealmDef extends AbstractRealmDef {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    private static final String REALM_ID = "vk";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private Application context;

    @Inject
    @Nonnull
    private ImageLoader imageLoader;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    private final HttpRealmIconService.UrlGetter iconUrlGetter = HttpRealmIconService.newUrlFromPropertyGetter("photo");

    @Nonnull
    private final HttpRealmIconService.UrlGetter photoUrlGetter = new VkPhotoUrlGetter();

    /*@Nonnull*/
    private volatile HttpRealmIconService iconService;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    public VkRealmDef() {
        super(REALM_ID, R.string.mpp_vk_realm_name, R.drawable.mpp_vk_icon, VkRealmConfigurationFragment.class, VkRealmConfiguration.class, false);
    }

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration, @Nonnull RealmState state) {
        return new VkRealm(realmId, this, user, (VkRealmConfiguration) configuration, state);
    }

    @Nonnull
    @Override
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return new VkRealmBuilder(this, editedRealm, (VkRealmConfiguration) configuration);
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        final List<AProperty> result = new ArrayList<AProperty>(user.getProperties().size());

        for (AProperty property : user.getProperties()) {
            final String name = property.getName();
            if ( name.equals(User.PROPERTY_NICKNAME) ) {
                addUserProperty(context, result, R.string.mpp_nickname, property.getValue());
            } else if ( name.equals(User.PROPERTY_SEX) ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_sex), context.getString(Gender.valueOf(property.getValue()).getCaptionResId())));
            } else if ( name.equals("bdate") ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_birth_date), property.getValue()));
            } else if ( name.equals("countryId") ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_country), property.getValue()));
            } else if ( name.equals("cityId") ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_city), property.getValue()));
            }

        }

        return result;
    }

    @Override
    public void init(@Nonnull Context context) {
        super.init(context);
    }

    @Nonnull
    @Override
    public synchronized RealmIconService getRealmIconService() {
        if (iconService == null) {
            iconService = new HttpRealmIconService(context, imageLoader, R.drawable.mpp_icon_user_empty, R.drawable.mpp_icon_users, iconUrlGetter, photoUrlGetter);
        }
        return iconService;
    }

    @Nullable
    @Override
    public Cipherer<RealmConfiguration, RealmConfiguration> getCipherer() {
        return new VkRealmConfigurationCipherer(Security.newAndroidAesStringCipherer());
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static final class VkPhotoUrlGetter implements HttpRealmIconService.UrlGetter {

        @Nullable
        @Override
        public String getUrl(@Nonnull User user) {
            String result = user.getPropertyValueByName("photoRec");

            if (result == null) {
                result = user.getPropertyValueByName("photoBig");
            }

            if ( result == null ) {
                result = user.getPropertyValueByName("photo");
            }

            return result;
        }
    }

    private static class VkRealmConfigurationCipherer implements Cipherer<RealmConfiguration, RealmConfiguration> {

        @Nonnull
        private final Cipherer<String, String> stringCipherer;

        private VkRealmConfigurationCipherer(@Nonnull Cipherer<String, String> stringCipherer) {
            this.stringCipherer = stringCipherer;
        }

        @Nonnull
        @Override
        public RealmConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull RealmConfiguration decrypted) throws CiphererException {
            return encrypt(secret, (VkRealmConfiguration)decrypted);
        }

        @Nonnull
        public RealmConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull VkRealmConfiguration decrypted) throws CiphererException {
            final VkRealmConfiguration encrypted = decrypted.clone();
            encrypted.setAccessParameters(stringCipherer.encrypt(secret, decrypted.getAccessToken()), decrypted.getUserId());
            return encrypted;
        }

        @Nonnull
        @Override
        public RealmConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull RealmConfiguration encrypted) throws CiphererException {
            return decrypt(secret, (VkRealmConfiguration)encrypted);
        }

        @Nonnull
        public RealmConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull VkRealmConfiguration encrypted) throws CiphererException {
            final VkRealmConfiguration decrypted = encrypted.clone();
            decrypted.setAccessParameters(stringCipherer.decrypt(secret, encrypted.getAccessToken()), encrypted.getUserId());
            return decrypted;
        }
    }
}
