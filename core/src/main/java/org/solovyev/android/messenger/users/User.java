package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface User extends MutableUserSyncData, MessengerEntity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */


    @Nonnull
    static final String PROPERTY_ONLINE = "online";

    @Nonnull
    static final String PROPERTY_FIRST_NAME = "first_name";

    @Nonnull
    static final String PROPERTY_LAST_NAME = "last_name";

    @Nonnull
    static final String PROPERTY_NICKNAME = "nick_name";


    /**
     * Property 'sex' must contain only string representations of enum {@link Gender}
     */
    @Nonnull
    static final String PROPERTY_SEX = "sex";

    @Nonnull
    static final String PROPERTY_PHONE = "phone";

    @Nonnull
    static final String PROPERTY_EMAIL = "email";


    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

    @Nonnull
    String getLogin();

    @Nullable
    Gender getGender();

    boolean isOnline();

    @Nonnull
    List<AProperty> getProperties();

    @Nonnull
    RealmEntity getRealmEntity();

    @Nullable
    String getPropertyValueByName(@Nonnull String name);

    @Nonnull
    UserSyncData getUserSyncData();

    @Nonnull
    User clone();

    @Nonnull
    User cloneWithNewStatus(boolean online);

    /*
    **********************************************************************
    *
    *                           UPDATE SYNC DATA
    *
    **********************************************************************
    */

    @Nonnull
    User updateChatsSyncDate();

    @Nonnull
    User updatePropertiesSyncDate();

    @Nonnull
    User updateContactsSyncDate();

    @Nonnull
    User updateUserIconsSyncDate();
}
