package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.realms.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:20 PM
 */
public abstract class AbstractXmppRealmService {

	@Nonnull
	private final XmppAccount realm;

	@Nonnull
	private final XmppConnectionAware connectionAware;

	protected AbstractXmppRealmService(@Nonnull XmppAccount realm, @Nonnull XmppConnectionAware connectionAware) {
		this.realm = realm;
		this.connectionAware = connectionAware;
	}

	@Nonnull
	public XmppAccount getRealm() {
		return realm;
	}

	protected <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws AccountConnectionException {
		try {
			return connectionAware.doOnConnection(callable);
		} catch (XMPPException e) {
			throw new AccountConnectionException(realm.getId(), e);
		}
	}
}
