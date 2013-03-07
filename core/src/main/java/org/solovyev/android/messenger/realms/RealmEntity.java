package org.solovyev.android.messenger.realms;

import android.os.Parcelable;
import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:10 PM
 */
public interface RealmEntity extends Parcelable {

    /**
     * @return unique ID of user in application
     */
    @Nonnull
    String getEntityId();

    /**
     * @return realm to which user is belonged to
     */
    @Nonnull
    String getRealmId();

    /**
     * @return realm def id to which user is belonged to
     */
    @Nonnull
    String getRealmDefId();

    /**
     * @return user id in realm
     */
    @Nonnull
    String getRealmEntityId();

    /*
    **********************************************************************
    *
    *                           EQUALS/HASHCODE/CLONE
    *
    **********************************************************************
    */

    int hashCode();

    boolean equals(Object o);

    @Nonnull
    RealmEntity clone();
}
