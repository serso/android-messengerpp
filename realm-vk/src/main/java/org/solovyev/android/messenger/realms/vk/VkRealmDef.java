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
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration) {
        return new VkRealm(realmId, this, user, (VkRealmConfiguration) configuration);
    }

    @Nonnull
    @Override
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return null;
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

    @Nonnull
    @Override
    public synchronized RealmIconService getRealmIconService() {
        if (iconService == null) {
            iconService = new HttpRealmIconService(context, imageLoader, R.drawable.mpp_icon_user_empty, R.drawable.mpp_icon_users, iconUrlGetter, photoUrlGetter);
        }
        return iconService;
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
}
