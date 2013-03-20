package org.solovyev.android.messenger.realms.vk.users;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 5/30/12
* Time: 10:06 PM
*/
class JsonUser {

    @Nullable
    private String uid;

    @Nullable
    private String first_name;

    @Nullable
    private String last_name;

    @Nullable
    private String nickname;

    @Nullable
    private Integer sex;

    @Nullable
    private Integer online;

    @Nullable
    private String bdate;

    @Nullable
    private Integer city;

    @Nullable
    private Integer country;

    @Nullable
    private String timezone;

    @Nullable
    private String photo;

    @Nullable
    private String photo_medium;

    @Nullable
    private String photo_big;

    @Nullable
    private String photo_rec;

    @Nonnull
    public User toUser(@Nonnull Realm realm) throws IllegalJsonException {
        if (uid == null) {
            throw new IllegalJsonException();
        }

        final List<AProperty> properties = new ArrayList<AProperty>();

        properties.add(Properties.newProperty(User.PROPERTY_FIRST_NAME, first_name));
        properties.add(Properties.newProperty(User.PROPERTY_LAST_NAME, last_name));
        properties.add(Properties.newProperty(User.PROPERTY_NICKNAME, nickname));

        final String gender = getGender();
        if (gender != null) {
            properties.add(Properties.newProperty(User.PROPERTY_SEX, gender));
        }

        final String onlineProperty = getOnline();
        if (onlineProperty != null) {
            properties.add(Properties.newProperty(User.PROPERTY_ONLINE, onlineProperty));
        }
        properties.add(Properties.newProperty("bdate", bdate));
        properties.add(Properties.newProperty("cityId", String.valueOf(city)));
        properties.add(Properties.newProperty("countryId", String.valueOf(country)));
        properties.add(Properties.newProperty("photo", photo));
        properties.add(Properties.newProperty("photoMedium", photo_medium));
        properties.add(Properties.newProperty("photoBig", photo_big));
        properties.add(Properties.newProperty("photoRec", photo_rec));

        return Users.newUser(realm.newUserEntity(uid), Users.newUserSyncData(DateTime.now(), null, null, null), properties);
    }

    @Nullable
    private String getOnline() {
        if ( online == null ) {
            return null;
        } else if ( online.equals(0) ) {
            return Boolean.FALSE.toString();
        } else if ( online.equals(1) ) {
            return Boolean.TRUE.toString();
        } else {
            return null;
        }
    }

    @Nullable
    private String getGender() {
        if ( sex == null ) {
            return null;
        } else if ( sex.equals(1) ) {
            return Gender.female.name();
        } else if ( sex.equals(2) ) {
            return Gender.male.name();
        } else {
            return null;
        }
    }

}
