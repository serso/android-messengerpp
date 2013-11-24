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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.solovyev.android.messenger.BaseInstrumentationTest;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class XmppAccountUserServiceTest extends BaseInstrumentationTest {

	@Nonnull
	private XmppAccount realm1;

	@Nonnull
	private XmppAccount realm2;

	@Inject
	@Nonnull
	private CustomXmppRealm xmppRealm;

	@Nonnull
	private AccountUserService accountUserService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		realm1 = newRealm1();
		realm2 = newRealm2();
		accountUserService = new XmppAccountUserService(realm1, TemporaryXmppConnectionAware.newInstance(realm1));
	}

	public void testGetUserById() throws Exception {
		final AccountConnection accountConnection2 = realm2.newConnection(getContext());

		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						accountConnection2.start();
					} catch (AccountConnectionException e) {
						throw new AccountRuntimeException(realm2.getId(), e);
					}
				}
			}).start();

			// wait until realm2 will be connected
			Thread.sleep(100);

			final User user2 = accountUserService.getUserById(realm2.getUser().getEntity().getAccountEntityId());
			assertNotNull(user2);
			assertEquals(user2.getEntity().getAccountEntityId(), TestXmppConfiguration.USER_LOGIN2);
			assertEquals(user2.getEntity().getAccountId(), realm1.getId());
			assertEquals("Sergey II Solovyev", user2.getFirstName());
			assertNull(user2.getLastName());

			// load self
			final User user1 = accountUserService.getUserById(realm1.getUser().getEntity().getAccountEntityId());
			assertNotNull(user1);
			assertEquals(user1.getEntity().getAccountEntityId(), TestXmppConfiguration.USER_LOGIN);
			assertEquals(user1.getEntity().getAccountId(), realm1.getId());
			assertEquals("Sergey I Solovyev", user1.getFirstName());
			assertNull(user1.getLastName());

			final User serso = accountUserService.getUserById("se.solovyev@gmail.com");
			assertNotNull(serso);
			assertEquals(serso.getEntity().getAccountEntityId(), "se.solovyev@gmail.com");
			assertEquals(serso.getEntity().getAccountId(), realm1.getId());
			assertEquals("Sergey", serso.getFirstName());
			assertEquals("Solovyev", serso.getLastName());


		} finally {
			accountConnection2.stop();
		}
	}

	public void testGetUserContacts() throws Exception {
		List<User> contacts1 = accountUserService.getUserContacts();
		assertTrue(contacts1.size() >= 2);
		assertTrue(Iterables.any(contacts1, new Predicate<User>() {
			@Override
			public boolean apply(@Nullable User contact) {
				return contact != null && contact.getEntity().getAccountEntityId().equals(TestXmppConfiguration.USER_LOGIN2);
			}
		}));
	}

	public void testCheckOnlineUsers() throws Exception {
		final AccountConnection accountConnection2 = realm2.newConnection(getContext());

		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						accountConnection2.start();
					} catch (AccountConnectionException e) {
						throw new AccountRuntimeException(realm2.getId(), e);
					}
				}
			}).start();

			// wait until realm2 will be connected
			Thread.sleep(100);


			final List<User> users1 = accountUserService.getOnlineUsers();
			assertNotNull(users1);
			assertTrue(!users1.isEmpty());


			assertTrue(Iterables.any(users1, new Predicate<User>() {
				@Override
				public boolean apply(@Nullable User contact) {
					return contact != null && contact.getEntity().getAccountEntityId().equals(TestXmppConfiguration.USER_LOGIN) && contact.isOnline();
				}
			}));

		} finally {
			accountConnection2.stop();
		}
	}

	public void testGetUserProperties() throws Exception {

	}


	@Nonnull
	protected XmppAccount newRealm1() {
		final String realmId = xmppRealm.getId() + "~01";
		XmppAccountConfiguration instance = TestXmppConfiguration.getInstance();
		return new XmppAccount(realmId, xmppRealm, Users.newEmptyUser(Entities.newEntity(realmId, instance.getLogin())), instance, AccountState.enabled);
	}

	@Nonnull
	protected XmppAccount newRealm2() {
		final String realmId = xmppRealm.getId() + "~02";
		XmppAccountConfiguration instance2 = TestXmppConfiguration.getInstance2();
		return new XmppAccount(realmId, xmppRealm, Users.newEmptyUser(Entities.newEntity(realmId, instance2.getLogin())), instance2, AccountState.enabled);
	}
}
