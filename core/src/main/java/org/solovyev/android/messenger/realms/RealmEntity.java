package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:10 PM
 */
public interface RealmEntity {

    /**
     * @return unique ID of user in application
     */
    @NotNull
    String getEntityId();

    /**
     * @return realm to which user is belonged to
     */
    @NotNull
    String getRealmId();

    /**
     * @return user id in realm
     */
    @NotNull
    String getRealmEntityId();

    @NotNull
    RealmEntity clone();
}
