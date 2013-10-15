package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.accounts.AbstractAccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.realms.xmpp.XmppAccountUserService.toAccountUser;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class XmppAccountBuilder extends AbstractAccountBuilder<XmppAccount, XmppAccountConfiguration> {

	@Nullable
	private Connection connection;

	public XmppAccountBuilder(@Nonnull Realm realm,
							  @Nullable XmppAccount editedAccount,
							  @Nonnull XmppAccountConfiguration configuration) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		MutableUser user;

		final String accountUserId = getConfiguration().getAccountUserId();

		if (connection != null) {
			try {
				user = toAccountUser(accountId, accountUserId, null, connection);
			} catch (XMPPException e) {
				Log.e("XmppRealmBuilder", e.getMessage(), e);
				user = newEmptyUser(Entities.newEntity(accountId, accountUserId));
			}
		} else {
			user = newEmptyUser(Entities.newEntity(accountId, accountUserId));
		}

		return user;
	}

	@Nonnull
	@Override
	protected XmppAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state) {
		return new XmppAccount(id, getRealm(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {
		connection = new XMPPConnection(getConfiguration().toXmppConfiguration());

		try {
			connection.connect();
		} catch (IllegalStateException e) {
			throw new ConnectionException(e);
		} catch (XMPPException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public void disconnect() throws ConnectionException {
		try {
			if (connection != null) {
				connection.disconnect();
			}
			connection = null;
		} catch (IllegalStateException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
		try {
			if (connection != null) {
				final XmppAccountConfiguration configuration = getConfiguration();
				connection.login(configuration.getLogin(), configuration.getPassword());
			} else {
				throw new InvalidCredentialsException("Not connected!");
			}
		} catch (XMPPException e) {
			throw new InvalidCredentialsException(e);
		}
	}
}
