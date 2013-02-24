package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:10 PM
 */
public interface RealmUser {

    /**
     * @return unique ID of user in application
     */
    @NotNull
    String getUserId();

    /**
     * @return realm to which user is belonged to
     */
    @NotNull
    String getRealmId();

    /**
     * @return user id in realm
     */
    @NotNull
    String getRealmUserId();

    @NotNull
    RealmUser clone();
}
