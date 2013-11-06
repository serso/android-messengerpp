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

import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

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
