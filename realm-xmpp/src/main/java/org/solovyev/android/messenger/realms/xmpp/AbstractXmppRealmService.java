package org.solovyev.android.messenger.realms.xmpp;

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

    @Nonnull
    private final XmppConnectionAware connectionAware;

    protected AbstractXmppRealmService(@Nonnull XmppRealm realm, @Nonnull XmppConnectionAware connectionAware) {
        this.realm = realm;
        this.connectionAware = connectionAware;
    }

    @Nonnull
    public XmppRealm getRealm() {
        return realm;
    }

    protected <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) {
        try {
            return connectionAware.doOnConnection(callable);
        } catch (XMPPException e) {
            throw new RealmIsNotConnectedException(e);
        }
    }
}
