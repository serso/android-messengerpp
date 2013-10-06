package org.solovyev.android.messenger.users;

import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.accounts.TestAccount;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.solovyev.android.messenger.users.Users.newNeverSyncedUserSyncData;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperties;

public class UserDaoTest extends DefaultDaoTest<User> {

	@Inject
	@Nonnull
	private UserDao dao;

	@Inject
	@Nonnull
	private ChatDao chatDao;

	@Nonnull
	@Override
	protected Dao<User> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected String getId(User user) {
		return user.getId();
	}

	@Test
	public void testShouldReadAllEntities() throws Exception {
		testShouldReadAllEntitiesForAccount(getAccountData1());
		testShouldReadAllEntitiesForAccount(getAccountData2());
		testShouldReadAllEntitiesForAccount(getAccountData3());
	}

	private void testShouldReadAllEntitiesForAccount(@Nonnull AccountData accountData) {
		final TestAccount account = accountData.getAccount();
		final List<User> contacts = accountData.getContacts();

		assertTrue(UsersTest.areSame(account.getUser(), dao.read(account.getUser().getId())));
		for(int i = 0; i < contacts.size(); i++) {
			final User user = dao.read(getEntityForContact(account, i).getEntityId());
			assertTrue(UsersTest.areSame(contacts.get(i), user));
		}
	}

	@Test
	public void testShouldRemoveContactsIfUserIsRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		dao.deleteById(userId);
		assertTrue(dao.readUserContacts(userId).isEmpty());
	}

	@Test
	public void testContactShouldBeRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		final List<String> contactIdsBefore = dao.readUserContactIds(userId);

		final String removeUserId = contactIdsBefore.get(0);
		dao.deleteById(removeUserId);
		contactIdsBefore.remove(removeUserId);

		final List<String> contactIdsAfter = dao.readUserContactIds(userId);
		assertEquals(contactIdsBefore, contactIdsAfter);
	}

	@Test
	public void testChatsShouldBeRemovedIfAccountUserRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		dao.deleteById(userId);

		assertTrue(chatDao.readUserChatIds(userId).isEmpty());
		assertFalse(chatDao.readUserChatIds(getAccount2().getUser().getId()).isEmpty());
	}

	@Test
	public void testPropertiesShouldBeRemovedIfUserRemoved() throws Exception {
		final String userId = getAccountData1().getContacts().get(0).getId();

		assertFalse(dao.readPropertiesById(userId).isEmpty());

		dao.deleteById(userId);

		assertTrue(dao.readPropertiesById(userId).isEmpty());
	}

	@Nonnull
	@Override
	protected Collection<User> populateEntities(@Nonnull Dao<User> dao) {
		final List<User> users = new ArrayList<User>();
		users.addAll(getUsers1());
		users.addAll(getUsers2());
		users.addAll(getUsers3());
		return users;
	}

	@Nonnull
	@Override
	protected Entity<User> newInsertEntity() {
		final MutableAProperties properties = newProperties(Collections.<String, AProperty>emptyMap());
		final org.solovyev.android.messenger.entities.Entity entity = getAccount1().newUserEntity("test");
		final User user = newUser(entity, newNeverSyncedUserSyncData(), properties);
		return newEntity(user);
	}

	@Override
	protected boolean areSame(@Nonnull User e1, @Nonnull User e2) {
		return UsersTest.areSame(e1, e2);
	}

	@Nonnull
	@Override
	protected User changeEntity(@Nonnull User user) {
		return user;
	}
}
