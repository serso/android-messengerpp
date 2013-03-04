package org.solovyev.android.messenger;

import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:52 PM
 */
public interface RealmConnection {

    @Nonnull
    Realm getRealm();

    void start();

    void stop();

    boolean isStopped();
}
