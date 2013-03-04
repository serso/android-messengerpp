package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:50 PM
 */
public class RealmIsNotConnectedException extends RuntimeException {

    public RealmIsNotConnectedException() {
    }

    public RealmIsNotConnectedException(@Nonnull Throwable e) {
        super(e);
    }
}
