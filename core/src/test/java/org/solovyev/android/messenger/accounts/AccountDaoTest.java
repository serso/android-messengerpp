package org.solovyev.android.messenger.accounts;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTestCase;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.Collection;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getLast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_app;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_user;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.properties.Properties.newProperty;

public class AccountDaoTest extends DefaultMessengerTestCase {

	@Nonnull
	@Inject
	private AccountDao dao;

	@Nonnull
	@Inject
	private TestRealm realm;

	@Test
	public void testShouldInsertAccount() throws Exception {
		final Account expected = new TestAccount(realm, 100).copyForNewState(disabled_by_user);
		dao.insertAccount(expected);

		final Account actual = findAccountInDao(expected);
		assertNotNull(actual);

		Accounts.assertEquals(expected, actual);
		assertEquals(disabled_by_user, actual.getState());
	}

	private Account findAccountInDao(final Account expected) {
		return find(dao.loadAccounts(), new Predicate<Account>() {
			@Override
			public boolean apply(@Nullable Account account) {
				return expected.equals(account);
			}
		}, null);
	}

	@Test
	public void testShouldDeleteExistingAccount() throws Exception {
		final Account account = new TestAccount(realm, 100);
		dao.insertAccount(account);
		dao.deleteAccount(account.getId());
		assertNull(findAccountInDao(account));
	}

	@Test
	public void testShouldDoNothingForNotExistingAccount() throws Exception {
		final Account account = new TestAccount(realm, 100);
		final Collection<Account> accountsBefore = dao.loadAccounts();

		dao.deleteAccount(account.getId());

		final Collection<Account> accountsAfter = dao.loadAccounts();
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
			dao.insertAccount(account);
			fail();
		} catch (RuntimeException e) {
			// todo serso: should be AccountException => check on device
			assertNull(findAccountInDao(account));
		}
	}

	@Test
	public void testShouldLoadAccounts() throws Exception {
		Accounts.assertEquals(getAccount1(), findAccountInDao(getAccount1()));
		Accounts.assertEquals(getAccount2(), findAccountInDao(getAccount2()));
		Accounts.assertEquals(getAccount3(), findAccountInDao(getAccount3()));
	}

	@Test
	public void testShouldOnlyLoadAccountsInState() throws Exception {
		final Account excepted = getAccount1().copyForNewState(disabled_by_app);
		dao.updateAccount(excepted);

		final Collection<Account> accountsInState = dao.loadAccountsInState(disabled_by_app);
		assertNotNull(accountsInState);
		assertEquals(1, accountsInState.size());
		Accounts.assertEquals(excepted, getLast(accountsInState));
	}

	@Test
	public void testShouldRemoveAllAccounts() throws Exception {
		dao.deleteAllAccounts();
		assertEquals(0, dao.loadAccounts().size());
	}

	@Test
	public void testShouldNotUpdateIfAccountDoesntExist() throws Exception {
		final Account account = new TestAccount(realm, 100);
		final Collection<Account> accountsBefore = dao.loadAccounts();

		dao.updateAccount(account);

		final Collection<Account> accountsAfter = dao.loadAccounts();
		assertEquals(accountsBefore, accountsAfter);
	}


	@Test
	public void testShouldUpdateState() throws Exception {
		final Account excepted = getAccount1().copyForNewState(disabled_by_app);
		dao.updateAccount(excepted);

		Accounts.assertEquals(excepted, findAccountInDao(excepted));
	}

	@Test
	public void testShouldUpdateConfiguration() throws Exception {
		final TestAccount excepted = getAccount1();
		final TestAccountConfiguration configuration = excepted.getConfiguration().clone();
		configuration.setAnotherTestStringField("tseadsgdgafdgadg");
		configuration.setTestStringField("3333");
		configuration.setTestIntField(2);
		excepted.setConfiguration(configuration);

		dao.updateAccount(excepted);
		Accounts.assertEquals(excepted, findAccountInDao(excepted));
	}
}
