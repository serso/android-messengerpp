package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

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

    @Nonnull
    private static final String REALM_ID = "vk";

    public VkRealmDef() {
        super(REALM_ID, R.string.mpp_vk_realm_name, R.drawable.mpp_vk_icon, VkRealmConfigurationFragment.class, VkRealmConfiguration.class, false);
    }

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration) {
        return new VkRealm(realmId, this, user, (VkRealmConfiguration) configuration);
    }

    @Nonnull
    @Override
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /*
    @Nonnull
    @Override
    public RealmAuthService newRealmAuthService(@Nonnull Realm realm) {
        return new VkRealmAuthService(login, password);
    }*/


    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        final List<AProperty> result = new ArrayList<AProperty>(user.getProperties().size());

        for (AProperty property : user.getProperties()) {
            final String name = property.getName();
            if ( name.equals(User.PROPERTY_NICKNAME) ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_nickname), property.getValue()));
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

    @Nullable
    @Override
    public String getUserIconUri(@Nonnull User user) {
        return user.getPropertyValueByName("photo");
    }
}
