package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:20 PM
 */
public abstract class AbstractXmppRealmService {

    @Nonnull
    private final XmppRealm realm;

    protected AbstractXmppRealmService(@Nonnull XmppRealm realm) {
        this.realm = realm;
    }

    @Nonnull
    public XmppRealm getRealm() {
        return realm;
    }

    protected <R> R doConnected(@Nonnull XmppConnectedCallable<R> callable) {
        final XmppRealmConfiguration configuration = getRealm().getConfiguration();

        final XMPPConnection connection = new XMPPConnection(configuration.toXmppConfiguration());
        try {
            connection.connect();
            connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());

            return callable.call(connection);
        } catch (XMPPException e) {
            throw new RealmIsNotConnectedException(e);
        } finally {
            connection.disconnect();
        }
    }
}
