package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;

import javax.annotation.Nonnull;

/**
* User: serso
* Date: 3/5/13
* Time: 9:21 PM
*/
interface XmppConnectedCallable<R> {

    R call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException;

}
