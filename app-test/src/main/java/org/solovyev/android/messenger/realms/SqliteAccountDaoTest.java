package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.BaseInstrumentationTest;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.sms.SmsRealm;
import org.solovyev.android.messenger.realms.vk.VkAccountConfiguration;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.realms.xmpp.CustomXmppRealm;
import org.solovyev.android.messenger.realms.xmpp.XmppAccountConfiguration;
import org.solovyev.android.messenger.users.Users;

import java.util.Collection;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.solovyev.android.messenger.realms.Realms.makeAccountId;

public class SqliteAccountDaoTest extends BaseInstrumentationTest {

	@Inject
	private AccountDao dao;

	@Inject
	private VkRealm vkRealm;

	@Inject
	private CustomXmppRealm xmppRealm;

	@Inject
	private SmsRealm smsRealm;

	public void setUp() throws Exception {
		super.setUp();
		dao.deleteAll();
	}

	public void testVkAccountShouldBeSaved() throws Exception {
		Collection<Account> accounts = dao.readAll();
		Assert.assertTrue(accounts.isEmpty());

		final VkAccountConfiguration expectedConfiguration = new VkAccountConfiguration("login", "password");
		expectedConfiguration.setAccessParameters("token", "user_id");
		final Account expectedAccount = vkRealm.newAccount("test~01", Users.newEmptyUser(Entities.newEntity("test~01", "user01")), expectedConfiguration, AccountState.enabled);
		dao.create(expectedAccount);

		accounts = dao.readAll();
		assertTrue(accounts.size() == 1);
		final Account<VkAccountConfiguration> actualAccount = getFirst(accounts, null);
		assertNotNull(actualAccount);
		assertTrue(expectedAccount.same(actualAccount));
		assertTrue(actualAccount.getConfiguration().isSame(expectedConfiguration));
		assertEquals("login", actualAccount.getConfiguration().getLogin());

		// password should be cleared as we have access token instead
		assertEquals("", actualAccount.getConfiguration().getPassword());
		assertEquals("token", actualAccount.getConfiguration().getAccessToken());
		assertEquals("user_id", actualAccount.getConfiguration().getUserId());

		dao.deleteById(expectedAccount.getId());

		accounts = dao.readAll();
		assertTrue(accounts.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (Realm realm : asList(vkRealm, xmppRealm, smsRealm)) {
			final AccountConfiguration configuration = (AccountConfiguration) realm.getConfigurationClass().newInstance();
			if (realm == vkRealm) {
				((VkAccountConfiguration) configuration).setAccessParameters("test", "test");
			} else if (realm == xmppRealm) {
				((XmppAccountConfiguration) configuration).setPassword("test");
			}
			final String accountId = makeAccountId(realm.getId(), index);
			final Account account = realm.newAccount(accountId, Users.newEmptyUser(Entities.newEntity(accountId, String.valueOf(index))), configuration, AccountState.enabled);
			dao.create(account);
		}

		final Collection<Account> accounts = dao.readAll();
		assertTrue(accounts.size() == 3);
	}

	public void tearDown() throws Exception {
		dao.deleteAll();
		super.tearDown();
	}
}
