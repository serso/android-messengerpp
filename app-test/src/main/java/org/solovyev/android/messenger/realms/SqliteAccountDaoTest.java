package org.solovyev.android.messenger.realms;

import junit.framework.Assert;

import java.util.Collection;

import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.collections.Collections;

import com.google.inject.Inject;

public class SqliteAccountDaoTest extends AbstractMessengerTestCase {

	@Inject
	private AccountDao accountDao;

	@Inject
	private TestRealm testRealmDef;

	public void setUp() throws Exception {
		super.setUp();
		accountDao.deleteAllAccounts();
	}

	public void testRealmOperations() throws Exception {
		Collection<Account> accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.isEmpty());

		TestAccountConfiguration expectedConfig1 = new TestAccountConfiguration("test_config_field", 42);
		final Account expected1 = testRealmDef.newAccount("test~01", Users.newEmptyUser(EntityImpl.newEntity("test~01", "user01")), expectedConfig1, AccountState.enabled);
		accountDao.insertAccount(expected1);

		accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.size() == 1);
		Account<TestAccountConfiguration> actual1 = Collections.getFirstCollectionElement(accounts);
		Assert.assertNotNull(actual1);
		Assert.assertTrue(expected1.same(actual1));
		Assert.assertTrue(actual1.getConfiguration().equals(expectedConfig1));
		Assert.assertEquals("test_config_field", actual1.getConfiguration().getTestStringField());
		Assert.assertEquals(42, actual1.getConfiguration().getTestIntField());

		accountDao.deleteAccount(expected1.getId());

		accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (Realm realm : getRealmService().getRealms()) {
			final AccountConfiguration accountConfiguration = (AccountConfiguration) realm.getConfigurationClass().newInstance();
			final String realmId = EntityImpl.getAccountId(realm.getId(), index);
			Account expected = realm.newAccount(realmId, Users.newEmptyUser(EntityImpl.newEntity(realmId, String.valueOf(index))), accountConfiguration, AccountState.enabled);
			accountDao.insertAccount(expected);
		}

		Collection<Account> accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.size() == 3);
	}

	public void tearDown() throws Exception {
		accountDao.deleteAllAccounts();
		super.tearDown();
	}
}
