package org.solovyev.android.messenger.realms;

import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:10 PM
 */
public interface RealmEntity extends Parcelable {

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
     * @return realm def id to which user is belonged to
     */
    @NotNull
    String getRealmDefId();

    /**
     * @return user id in realm
     */
    @NotNull
    String getRealmEntityId();

    @NotNull
    RealmEntity clone();

    int hashCode();

    boolean equals(Object o);
}
