package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.RealmConnection;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:05 PM
 */
public class TestRealmConnection implements RealmConnection {

    @Nonnull
    private final TestRealm realm;

    public TestRealmConnection(@Nonnull TestRealm realm) {
        this.realm = realm;
    }

    @Nonnull
    @Override
    public Realm getRealm() {
        return this.realm;
    }

    @Override
    public void start() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isStopped() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
