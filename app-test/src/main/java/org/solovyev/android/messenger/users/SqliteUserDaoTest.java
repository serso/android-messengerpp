package org.solovyev.android.messenger.users;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.realms.TestAccount;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.ListEqualizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:50 PM
 */
public class SqliteUserDaoTest extends AbstractMessengerTestCase {

	private static final int REALMS_COUNT = 10;

	@Inject
	private UserDao userDao;

	@Inject
	private TestRealm testRealmDef;

	@Inject
	private TestAccount testRealm;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		userDao.deleteAllUsers();
	}

	public void testUserOperations() throws Exception {
		// INSERT

		List<AProperty> expectedProperties = new ArrayList<AProperty>();
		expectedProperties.add(Properties.newProperty("prop_1", "prop_1_value"));
		expectedProperties.add(Properties.newProperty("prop_2", "prop_2_value"));
		expectedProperties.add(Properties.newProperty("prop_3", "prop_3_value"));

		final Entity realmUser = testRealm.newUserEntity("2");

		User expected = Users.newUser(realmUser, Users.newNeverSyncedUserSyncData(), expectedProperties);

		userDao.insertUser(expected);
		userDao.insertUser(expected);

		User actual = userDao.loadUserById("test~1:2");

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(realmUser.getAccountId(), actual.getEntity().getAccountId());
		Assert.assertEquals("2", actual.getEntity().getRealmEntityId());
		Assert.assertEquals("test~1:2", actual.getEntity().getEntityId());
		Assert.assertTrue(Objects.areEqual(expectedProperties, actual.getProperties(), new CollectionEqualizer<AProperty>(null)));
		Assert.assertEquals("prop_1_value", actual.getPropertyValueByName("prop_1"));

		User actual2 = userDao.loadUserById("test~1:2");
		Assert.assertEquals(expected, actual2);

		User actual3 = userDao.loadUserById("test_01");
		Assert.assertNull(actual3);

		Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2"), ListEqualizer.<String>newWithNaturalEquals(false)));

		final Entity realmUser2 = testRealm.newUserEntity("3");

		expected = Users.newUser(realmUser2, Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.insertUser(expected);
		Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

		// UPDATE
		expectedProperties = new ArrayList<AProperty>(expectedProperties);
		expectedProperties.remove(0);
		expectedProperties.add(Properties.newProperty("prop_4", "prop_4_value"));

		expected = Users.newUser(realmUser, Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.updateUser(expected);
		actual = userDao.loadUserById("test~1:2");

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertTrue(Objects.areEqual(expectedProperties, actual.getProperties(), new CollectionEqualizer<AProperty>(null)));

		Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

		expected = Users.newUser(TestRealm.REALM_ID, "test_01dsfsdfsf", Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.updateUser(expected);

		List<String> usersIds = userDao.loadUserIds();
		Assert.assertEquals(2, usersIds.size());
		userDao.deleteAllUsersForAccount("test~1");
		usersIds = userDao.loadUserIds();
		Assert.assertEquals(0, usersIds.size());
	}

	public void testRandomOperations() throws Exception {
		final List<Account> accounts = new ArrayList<Account>(REALMS_COUNT);
		for (int i = 0; i < REALMS_COUNT; i++) {
			accounts.add(new TestAccount(testRealmDef, i));
		}

		final List<User> users = new ArrayList<User>();

		final Random random = new Random(new Date().getTime());
		for (int i = 0; i < 1000; i++) {
			final int operation = random.nextInt(7);
			switch (operation) {
				case 0:
				case 1:
				case 2:
				case 3:
					final Account r = accounts.get(random.nextInt(REALMS_COUNT));
					List<AProperty> newProperties = generateUserProperties(random);
					final User newUser = Users.newUser(r.newUserEntity("user" + String.valueOf(i)), Users.newNeverSyncedUserSyncData(), newProperties);
					users.add(newUser);
					userDao.insertUser(newUser);
					Assert.assertTrue(Objects.areEqual(newUser, userDao.loadUserById(newUser.getId())));
					Assert.assertTrue(Objects.areEqual(newProperties, userDao.loadUserPropertiesById(newUser.getId()), new CollectionEqualizer<AProperty>(null)));
					break;
				case 4:
					if (!users.isEmpty()) {
						int userPosition = random.nextInt(2 * users.size());

						if (userPosition < users.size()) {
							User updatedUser = users.get(userPosition);
							List<AProperty> updatedProperties = generateUserProperties(random);
							updatedUser = Users.newUser(updatedUser.getEntity(), Users.newNeverSyncedUserSyncData(), updatedProperties);
							users.set(userPosition, updatedUser);
							userDao.updateUser(updatedUser);
							Assert.assertTrue(Objects.areEqual(updatedProperties, userDao.loadUserPropertiesById(updatedUser.getId()), new CollectionEqualizer<AProperty>(null)));
						} else {
							final Account r2 = accounts.get(random.nextInt(REALMS_COUNT));
							final User newUser2 = Users.newUser(r2.newUserEntity("user" + String.valueOf(i)), Users.newNeverSyncedUserSyncData(), generateUserProperties(random));
							userDao.updateUser(newUser2);
							Assert.assertNull(userDao.loadUserById(newUser2.getId()));
							Assert.assertTrue(userDao.loadUserPropertiesById(newUser2.getId()).isEmpty());
						}
					}
					break;
				case 5:
					final Account account = accounts.get(random.nextInt(accounts.size()));
					Iterables.removeIf(users, new Predicate<User>() {
						@Override
						public boolean apply(@Nullable User user) {
							return user.getEntity().getAccountId().equals(account.getId());
						}
					});
					userDao.deleteAllUsersForAccount(account.getId());
					break;
				case 6:
					users.clear();
					userDao.deleteAllUsers();
					break;
			}

			final List<String> userIds = userDao.loadUserIds();
			Assert.assertEquals(users.size(), userIds.size());
			for (User user : users) {
				Assert.assertTrue(userIds.contains(user.getId()));
			}

		}

	}

	@Nonnull
	private List<AProperty> generateUserProperties(@Nonnull Random random) {
		final int count = random.nextInt(10);
		final List<AProperty> result = new ArrayList<AProperty>(count);
		for (int i = 0; i < count; i++) {
			result.add(Properties.newProperty("name" + i, "value" + i));
		}
		return result;
	}

	@Override
	public void tearDown() throws Exception {
		userDao.deleteAllUsers();
		super.tearDown();
	}
}
