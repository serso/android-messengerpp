package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:12 AM
 */
public class UnsupportedRealmException extends RealmException {

    public UnsupportedRealmException(@Nonnull String realm) {
        super("Realm " + realm + " is not supported!");
    }
}
