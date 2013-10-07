package org.solovyev.android.messenger.users;

import com.google.common.base.Function;
import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.accounts.TestAccount;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
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

	public UserDaoTest() {
		super(new UserSameEqualizer());
	}

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

		assertTrue(areSame(account.getUser(), dao.read(account.getUser().getId())));
		for(int i = 0; i < contacts.size(); i++) {
			final User user = dao.read(getEntityForContact(account, i).getEntityId());
			assertTrue(areSame(contacts.get(i), user));
		}
	}

	@Test
	public void testShouldRemoveContactsIfUserIsRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		dao.deleteById(userId);
		assertTrue(dao.readContacts(userId).isEmpty());
	}

	@Test
	public void testContactShouldBeRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		final Collection<String> contactIdsBefore = dao.readLinkedEntityIds(userId);

		final String removeUserId = getFirst(contactIdsBefore, null);
		dao.deleteById(removeUserId);
		contactIdsBefore.remove(removeUserId);

		final Collection<String> contactIdsAfter = dao.readLinkedEntityIds(userId);
		assertEquals(contactIdsBefore, contactIdsAfter);
	}

	@Test
	public void testChatsShouldBeRemovedIfAccountUserRemoved() throws Exception {
		final String userId = getAccount1().getUser().getId();
		dao.deleteById(userId);

		assertTrue(chatDao.readLinkedEntityIds(userId).isEmpty());
		assertFalse(chatDao.readLinkedEntityIds(getAccount2().getUser().getId()).isEmpty());
	}

	@Test
	public void testPropertiesShouldBeRemovedIfUserRemoved() throws Exception {
		final String userId = getAccountData1().getContacts().get(0).getId();

		assertFalse(dao.readPropertiesById(userId).isEmpty());

		dao.deleteById(userId);

		assertTrue(dao.readPropertiesById(userId).isEmpty());
	}


	@Test
	public void testShouldReadAllContactsForUser() throws Exception {
		for (AccountData accountData : getAccountDataList()) {
			final String userId = accountData.getAccount().getUser().getId();

			final List<User> contactsFromDao = dao.readContacts(userId);
			assertEntitiesSame(contactsFromDao, accountData.getContacts());
		}
	}


	@Test
	public void testShouldReadAllContactIdsForUser() throws Exception {
		for (AccountData accountData : getAccountDataList()) {
			final String userId = accountData.getAccount().getUser().getId();

			final Collection<String> contactIdsFromDao = dao.readLinkedEntityIds(userId);
			Objects.areEqual(contactIdsFromDao, newArrayList(transform(accountData.getContacts(), new Function<User, String>() {
				@Override
				public String apply(@Nullable User contact) {
					return contact.getId();
				}
			})), new CollectionEqualizer<String>(null));
		}
	}

	@Test
	public void testShouldChangeUserStatuses() throws Exception {
		for (User user : getAccountData1().getUsers()) {
			dao.updateOnlineStatus(user.cloneWithNewStatus(!user.isOnline()));
			final User userFromDb = dao.read(user.getId());
			assertNotNull(userFromDb);
			assertTrue(user.isOnline() != userFromDb.isOnline());
		}
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

	@Nonnull
	@Override
	protected User changeEntity(@Nonnull User user) {
		return user;
	}
}
