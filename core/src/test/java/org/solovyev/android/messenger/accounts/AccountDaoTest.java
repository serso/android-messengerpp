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

package org.solovyev.android.messenger.accounts;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.realms.UserSavingAccountDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getLast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_app;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_user;

public class AccountDaoTest extends DefaultDaoTest<Account> {

	@Nonnull
	@Inject
	private AccountDao dao;

	@Nonnull
	@Inject
	private UserDao userDao;

	@Nonnull
	@Inject
	private TestRealm realm;

	@Nonnull
	@Inject
	private UserService userService;

	public AccountDaoTest() {
		super(new AccountSameEqualizer());
	}

	@Test
	public void testShouldInsertAccount() throws Exception {
		final Account expected = new TestAccount(realm, 100).copyForNewState(disabled_by_user);
		getDao().create(expected);

		final Account actual = findAccountInDao(expected);
		assertNotNull(actual);

		AccountsTest.assertEquals(expected, actual);
		assertEquals(disabled_by_user, actual.getState());
	}

	private Account findAccountInDao(final Account expected) {
		return find(dao.readAll(), new Predicate<Account>() {
			@Override
			public boolean apply(@Nullable Account account) {
				return expected.equals(account);
			}
		}, null);
	}

	@Test
	public void testShouldDeleteExistingAccount() throws Exception {
		final Account account = new TestAccount(realm, 100);
		getDao().create(account);
		dao.deleteById(account.getId());
		assertNull(findAccountInDao(account));
	}

	@Test
	public void testShouldDoNothingForNotExistingAccount() throws Exception {
		final Account account = new TestAccount(realm, 100);
		final Collection<Account> accountsBefore = dao.readAll();

		dao.deleteById(account.getId());

		final Collection<Account> accountsAfter = dao.readAll();
		assertEquals(accountsBefore, accountsAfter);
	}

	@Test
	public void testShouldThrowAccountExceptionInCaseOfCiphererException() throws Exception {
		final TestRealm realmsSpy = spy(realm);
		when(realmsSpy.getCipherer()).thenReturn(new Cipherer<TestAccountConfiguration, TestAccountConfiguration>() {
			@Nonnull
			@Override
			public TestAccountConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull TestAccountConfiguration decrypted) throws CiphererException {
				throw new CiphererException();
			}

			@Nonnull
			@Override
			public TestAccountConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull TestAccountConfiguration encrypted) throws CiphererException {
				throw new CiphererException();
			}
		});

		final Account account = new TestAccount(realmsSpy, 100);
		try {
			dao.create(account);
			fail();
		} catch (RuntimeException e) {
			// todo serso: should be AccountException => check on device
			assertNull(findAccountInDao(account));
		}
	}

	@Test
	public void testShouldLoadAccounts() throws Exception {
		AccountsTest.assertEquals(getAccount1(), findAccountInDao(getAccount1()));
		AccountsTest.assertEquals(getAccount2(), findAccountInDao(getAccount2()));
		AccountsTest.assertEquals(getAccount3(), findAccountInDao(getAccount3()));
	}

	@Test
	public void testShouldOnlyLoadAccountsInState() throws Exception {
		final Account excepted = getAccount1().copyForNewState(disabled_by_app);
		dao.update(excepted);

		final Collection<Account> accountsInState = dao.loadAccountsInState(disabled_by_app);
		assertNotNull(accountsInState);
		assertEquals(1, accountsInState.size());
		AccountsTest.assertEquals(excepted, getLast(accountsInState));
	}

	@Test
	public void testShouldRemoveAllAccounts() throws Exception {
		dao.deleteAll();
		assertEquals(0, dao.readAll().size());
	}

	@Test
	public void testShouldNotUpdateIfAccountDoesntExist() throws Exception {
		final Account account = new TestAccount(realm, 100);
		final Collection<Account> accountsBefore = dao.readAll();

		dao.update(account);

		final Collection<Account> accountsAfter = dao.readAll();
		assertEquals(accountsBefore, accountsAfter);
	}


	@Test
	public void testShouldUpdateState() throws Exception {
		final Account excepted = getAccount1().copyForNewState(disabled_by_app);
		dao.update(excepted);

		AccountsTest.assertEquals(excepted, findAccountInDao(excepted));
	}

	@Test
	public void testShouldUpdateConfiguration() throws Exception {
		final TestAccount excepted = getAccount1();
		final TestAccountConfiguration configuration = excepted.getConfiguration().clone();
		configuration.setAnotherTestStringField("tseadsgdgafdgadg");
		configuration.setTestStringField("3333");
		configuration.setTestIntField(2);
		excepted.setConfiguration(configuration);

		dao.update(excepted);
		AccountsTest.assertEquals(excepted, findAccountInDao(excepted));
	}

	@Test
	public void testShouldDeleteUsersIfAccountIsRemoved() throws Exception {
		final TestAccount account1 = getAccount1();
		dao.deleteById(account1.getId());

		assertTrue(userDao.readLinkedEntityIds(account1.getUser().getId()).isEmpty());
		assertNull(userDao.read(account1.getUser().getId()));
	}

	@Nonnull
	@Override
	protected Dao<Account> getDao() {
		return new UserSavingAccountDao(userService, dao);
	}

	@Nonnull
	@Override
	protected String getId(Account account) {
		return account.getId();
	}

	@Nonnull
	@Override
	protected Collection<Account> populateEntities(@Nonnull Dao<Account> dao) {
		return Arrays.<Account>asList(getAccount1(), getAccount2(), getAccount3());
	}

	@Nonnull
	@Override
	protected DaoEntity<Account> newInsertEntity() {
		final Account account = new TestAccount(realm, 100);
		return newEntity(account, account.getId());
	}

	@Nonnull
	@Override
	protected Account changeEntity(@Nonnull Account entity) {
		return entity.copyForNewState(AccountState.disabled_by_user);
	}
}
