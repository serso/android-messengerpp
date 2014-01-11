/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.accounts.AbstractAccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.realms.xmpp.XmppAccountUserService.toAccountUser;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class XmppAccountBuilder extends AbstractAccountBuilder<XmppAccount, XmppAccountConfiguration> {

	@Nullable
	private Connection connection;

	public XmppAccountBuilder(@Nonnull XmppRealm realm,
							  @Nullable XmppAccount editedAccount,
							  @Nonnull XmppAccountConfiguration configuration) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	public XmppRealm getRealm() {
		return (XmppRealm) super.getRealm();
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
				Log.e(XmppRealm.TAG, e.getMessage(), e);
				user = newEmptyUser(Entities.newEntity(accountId, accountUserId));
			}
		} else {
			user = newEmptyUser(Entities.newEntity(accountId, accountUserId));
		}

		return user;
	}

	@Nonnull
	@Override
	protected XmppAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new XmppAccount(id, getRealm(), user, getConfiguration(), state, syncData);
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
				connection.login(configuration.getLoginForConnection(), configuration.getPassword());
			} else {
				throw new InvalidCredentialsException("Not connected!");
			}
		} catch (XMPPException e) {
			throw new InvalidCredentialsException(e);
		}
	}
}
