package org.solovyev.android.messenger.accounts;

import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.UserDao;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.accounts.AccountState.removed;

public class AccountServiceTest extends DefaultMessengerTest {

	@Inject
	@Nonnull
	private TestRealm realm;

	@Inject
	@Nonnull
	private AccountService service;

	@Inject
	@Nonnull
	private AccountDao dao;

	@Inject
	@Nonnull
	private UserDao userDao;

	@Test
	public void testShouldRemoveAccountsInRemovedStateAfterInit() throws Exception {
		final TestAccount account = getAccount1();

		service.changeAccountState(account, removed);
		service.init();

		assertNull(dao.read(account.getId()));
		assertNull(userDao.read(account.getUser().getId()));
		assertTrue(userDao.readContacts(account.getUser().getId()).isEmpty());
	}

	@Test
	public void testAccountUserShouldBeSavedOnAccountSave() throws Exception {
		final TestAccount account = service.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration(34), null));
		assertEquals("test_user_34", account.getUser().getEntity().getAccountEntityId());
		assertNotNull(userDao.read(account.getUser().getEntity().getEntityId()));
	}
}
