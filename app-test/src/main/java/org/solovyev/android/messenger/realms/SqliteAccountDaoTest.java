package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.collections.Collections;

import java.util.Collection;

public class SqliteAccountDaoTest extends AbstractMessengerTestCase {

	@Inject
	private AccountDao accountDao;

	@Inject
	private TestRealmDef testRealmDef;

	public void setUp() throws Exception {
		super.setUp();
		accountDao.deleteAllAccounts();
	}

	public void testRealmOperations() throws Exception {
		Collection<Account> accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.isEmpty());

		TestAccountConfiguration expectedConfig1 = new TestAccountConfiguration("test_config_field", 42);
		final Account expected1 = testRealmDef.newRealm("test~01", Users.newEmptyUser(EntityImpl.newInstance("test~01", "user01")), expectedConfig1, AccountState.enabled);
		accountDao.insertRealm(expected1);

		accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.size() == 1);
		Account<TestAccountConfiguration> actual1 = Collections.getFirstCollectionElement(accounts);
		Assert.assertNotNull(actual1);
		Assert.assertTrue(expected1.same(actual1));
		Assert.assertTrue(actual1.getConfiguration().equals(expectedConfig1));
		Assert.assertEquals("test_config_field", actual1.getConfiguration().getTestStringField());
		Assert.assertEquals(42, actual1.getConfiguration().getTestIntField());

		accountDao.deleteRealm(expected1.getId());

		accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (RealmDef realmDef : getAccountService().getRealmDefs()) {
			final AccountConfiguration accountConfiguration = (AccountConfiguration) realmDef.getConfigurationClass().newInstance();
			final String realmId = EntityImpl.getRealmId(realmDef.getId(), index);
			Account expected = realmDef.newRealm(realmId, Users.newEmptyUser(EntityImpl.newInstance(realmId, String.valueOf(index))), accountConfiguration, AccountState.enabled);
			accountDao.insertRealm(expected);
		}

		Collection<Account> accounts = accountDao.loadAccounts();
		Assert.assertTrue(accounts.size() == 3);
	}

	public void tearDown() throws Exception {
		accountDao.deleteAllAccounts();
		super.tearDown();
	}
}
