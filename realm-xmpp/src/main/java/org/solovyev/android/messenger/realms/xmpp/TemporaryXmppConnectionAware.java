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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

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
