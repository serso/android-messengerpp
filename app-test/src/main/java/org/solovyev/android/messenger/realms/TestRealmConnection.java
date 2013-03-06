package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.AbstractRealmConnection;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:05 PM
 */
public class TestRealmConnection extends AbstractRealmConnection<TestRealm> {

    public TestRealmConnection(@Nonnull TestRealm realm, @Nonnull Context context) {
        super(realm, context);
    }

    @Override
    protected void doWork() {

    }

    @Override
    protected void stopWork() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
