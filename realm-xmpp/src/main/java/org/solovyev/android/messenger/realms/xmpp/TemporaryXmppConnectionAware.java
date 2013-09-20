package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 10:19 PM
 */

/**
 * Temporary class which is used when realm xmpp connection is not established yet
 */
class TemporaryXmppConnectionAware implements XmppConnectionAware {

	@Nonnull
	private final XmppAccount realm;

	private TemporaryXmppConnectionAware(@Nonnull XmppAccount realm) {
		this.realm = realm;
	}

	@Nonnull
	static XmppConnectionAware newInstance(@Nonnull XmppAccount realm) {
		return new TemporaryXmppConnectionAware(realm);
	}

	@Override
	public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException, AccountConnectionException {
		final Connection connection = new XMPPConnection(realm.getConfiguration().toXmppConfiguration());
		XmppAccountConnection.checkConnectionStatus(connection, realm);
		return callable.call(connection);
	}
}
