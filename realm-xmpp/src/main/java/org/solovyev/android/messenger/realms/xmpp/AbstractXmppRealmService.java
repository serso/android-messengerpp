package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:20 PM
 */
public abstract class AbstractXmppRealmService {

	@Nonnull
	private final XmppAccount account;

	@Nonnull
	private final XmppConnectionAware connectionAware;

	protected AbstractXmppRealmService(@Nonnull XmppAccount account, @Nonnull XmppConnectionAware connectionAware) {
		this.account = account;
		this.connectionAware = connectionAware;
	}

	@Nonnull
	public XmppAccount getAccount() {
		return account;
	}

	protected <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws AccountConnectionException {
		try {
			return connectionAware.doOnConnection(callable);
		} catch (XMPPException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}
}
