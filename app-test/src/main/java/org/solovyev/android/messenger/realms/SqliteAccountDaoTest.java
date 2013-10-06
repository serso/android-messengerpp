package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.collections.Collections;

import java.util.Collection;

import static org.solovyev.android.messenger.realms.Realms.makeAccountId;

public class SqliteAccountDaoTest extends AbstractMessengerTestCase {

	@Inject
	private AccountDao dao;

	@Inject
	private TestRealm testRealm;

	public void setUp() throws Exception {
		super.setUp();
		dao.deleteAll();
	}

	public void testRealmOperations() throws Exception {
		Collection<Account> accounts = dao.readAll();
		Assert.assertTrue(accounts.isEmpty());

		TestAccountConfiguration expectedConfig1 = new TestAccountConfiguration("test_config_field", 42);
		final Account expected1 = testRealm.newAccount("test~01", Users.newEmptyUser(Entities.newEntity("test~01", "user01")), expectedConfig1, AccountState.enabled);
		dao.create(expected1);

		accounts = dao.readAll();
		Assert.assertTrue(accounts.size() == 1);
		Account<TestAccountConfiguration> actual1 = Collections.getFirstCollectionElement(accounts);
		Assert.assertNotNull(actual1);
		Assert.assertTrue(expected1.same(actual1));
		Assert.assertTrue(actual1.getConfiguration().isSame(expectedConfig1));
		Assert.assertEquals("test_config_field", actual1.getConfiguration().getTestStringField());
		Assert.assertEquals(42, actual1.getConfiguration().getTestIntField());

		dao.deleteById(expected1.getId());

		accounts = dao.readAll();
		Assert.assertTrue(accounts.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (Realm realm : getRealmService().getRealms()) {
			final AccountConfiguration accountConfiguration = (AccountConfiguration) realm.getConfigurationClass().newInstance();
			final String accountId = makeAccountId(realm.getId(), index);
			Account expected = realm.newAccount(accountId, Users.newEmptyUser(Entities.newEntity(accountId, String.valueOf(index))), accountConfiguration, AccountState.enabled);
			dao.create(expected);
		}

		Collection<Account> accounts = dao.readAll();
		Assert.assertTrue(accounts.size() == 3);
	}

	public void tearDown() throws Exception {
		dao.deleteAll();
		super.tearDown();
	}
}
