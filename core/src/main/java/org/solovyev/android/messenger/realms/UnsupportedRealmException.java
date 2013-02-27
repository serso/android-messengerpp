package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:12 AM
 */
public class UnsupportedRealmException extends RuntimeException {

    public UnsupportedRealmException(@NotNull String realm) {
        super("Realm " + realm + " is not supported!");
    }
}
