package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 3:38 PM
 */
public interface XmppConnectionAware {

	<R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException, AccountConnectionException;
}
