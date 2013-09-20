package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:21 PM
 */
interface XmppConnectedCallable<R> {

	R call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException;

}
