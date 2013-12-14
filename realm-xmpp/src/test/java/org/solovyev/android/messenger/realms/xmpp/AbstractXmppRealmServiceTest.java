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
import org.jivesoftware.smack.XMPPException;
import org.junit.Test;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.Accounts;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class AbstractXmppRealmServiceTest {

	@Test
	public void testShouldRethrowAccountException() throws Exception {
		final XmppAccount account = new XmppAccount("test", mock(Realm.class), Users.newEmptyUser("test:test"), new XmppAccountConfiguration(), AccountState.enabled, Accounts.newNeverSyncedData());
		final TestXmppAccountService service = new TestXmppAccountService(account, new XmppConnectionAware() {
			@Override
			public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException, AccountConnectionException {
				callable.call(mock(Connection.class));
				return null;
			}
		});

		try {
			service.throwNpeOnConnection();
			fail();
		} catch (AccountConnectionException e) {
			if (!(e.getCause() instanceof NullPointerException)) {
				throw e;
			}
		}
	}

	private static class TestXmppAccountService extends AbstractXmppRealmService {

		protected TestXmppAccountService(@Nonnull XmppAccount account, @Nonnull XmppConnectionAware connectionAware) {
			super(account, connectionAware);
		}

		public void throwNpeOnConnection() throws AccountConnectionException {
			doOnConnection(new XmppConnectedCallable<Object>() {
				@Override
				public Object call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
					throw new NullPointerException("test");
				}
			});
		}
	}
}
