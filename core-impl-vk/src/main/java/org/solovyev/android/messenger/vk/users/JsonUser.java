package org.solovyev.android.messenger.vk.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.AProperty;
import org.solovyev.android.APropertyImpl;
import org.solovyev.android.VersionedEntityImpl;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserSyncDataImpl;

import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 5/30/12
* Time: 10:06 PM
*/
class JsonUser {

    @Nullable
    private Integer uid;

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

    @NotNull
    public User toUser() throws IllegalJsonException {
        if (uid == null) {
            throw new IllegalJsonException();
        }

        final List<AProperty> properties = new ArrayList<AProperty>();

        properties.add(APropertyImpl.newInstance("firstName", first_name));
        properties.add(APropertyImpl.newInstance("lastName", last_name));
        properties.add(APropertyImpl.newInstance("nickName", nickname));

        final String gender = getGender();
        if (gender != null) {
            properties.add(APropertyImpl.newInstance("sex", gender));
        }

        final String onlineProperty = getOnline();
        if (onlineProperty != null) {
            properties.add(APropertyImpl.newInstance("online", onlineProperty));
        }
        properties.add(APropertyImpl.newInstance("bdate", bdate));
        properties.add(APropertyImpl.newInstance("cityId", String.valueOf(city)));
        properties.add(APropertyImpl.newInstance("countryId", String.valueOf(country)));
        properties.add(APropertyImpl.newInstance("photo", photo));
        properties.add(APropertyImpl.newInstance("photoMedium", photo_medium));
        properties.add(APropertyImpl.newInstance("photoBig", photo_big));
        properties.add(APropertyImpl.newInstance("photoRec", photo_rec));

        return UserImpl.newInstance(new VersionedEntityImpl(uid), UserSyncDataImpl.newInstance(DateTime.now(), null, null, null), properties);
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
