package org.solovyev.android.messenger.users;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.TestAccount;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.ListEqualizer;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.users.Users.newNeverSyncedUserSyncData;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.common.Objects.areEqual;

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
	private TestRealm testRealm;

	@Inject
	private TestAccount testAccount;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		userDao.deleteAll();
	}

	public void testUserOperations() throws Exception {
		// INSERT

		List<AProperty> expectedProperties = new ArrayList<AProperty>();
		expectedProperties.add(Properties.newProperty("prop_1", "prop_1_value"));
		expectedProperties.add(Properties.newProperty("prop_2", "prop_2_value"));
		expectedProperties.add(Properties.newProperty("prop_3", "prop_3_value"));

		final Entity realmUser = testAccount.newUserEntity("2");

		User expected = newUser(realmUser, newNeverSyncedUserSyncData(), expectedProperties);

		userDao.create(expected);
		userDao.create(expected);

		User actual = userDao.read("test~1:2");

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(realmUser.getAccountId(), actual.getEntity().getAccountId());
		Assert.assertEquals("2", actual.getEntity().getAccountEntityId());
		Assert.assertEquals("test~1:2", actual.getEntity().getEntityId());
		Assert.assertTrue(areEqual(expectedProperties, actual.getPropertiesCollection(), new CollectionEqualizer<AProperty>(null)));
		Assert.assertEquals("prop_1_value", actual.getPropertyValueByName("prop_1"));

		User actual2 = userDao.read("test~1:2");
		Assert.assertEquals(expected, actual2);

		User actual3 = userDao.read("test_01");
		Assert.assertNull(actual3);

		Assert.assertTrue(areEqual(newArrayList(userDao.readAllIds()), Arrays.asList("test~1:2"), ListEqualizer.<String>newWithNaturalEquals(false)));

		final Entity realmUser2 = testAccount.newUserEntity("3");

		expected = newUser(realmUser2, Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.create(expected);
		Assert.assertTrue(areEqual(newArrayList(userDao.readAllIds()), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

		// UPDATE
		expectedProperties = new ArrayList<AProperty>(expectedProperties);
		expectedProperties.remove(0);
		expectedProperties.add(Properties.newProperty("prop_4", "prop_4_value"));

		expected = newUser(realmUser, Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.update(expected);
		actual = userDao.read("test~1:2");

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertTrue(areEqual(expectedProperties, actual.getPropertiesCollection(), new CollectionEqualizer<AProperty>(null)));

		Assert.assertTrue(areEqual(Lists.newArrayList(userDao.readAllIds()), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

		expected = newUser(TestRealm.REALM_ID, "test_01dsfsdfsf", Users.newUserSyncData(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
		userDao.update(expected);

		Collection<String> usersIds = userDao.readAllIds();
		Assert.assertEquals(2, usersIds.size());
	}

	public void testRandomOperations() throws Exception {
		final List<Account> accounts = new ArrayList<Account>(REALMS_COUNT);
		for (int i = 0; i < REALMS_COUNT; i++) {
			accounts.add(new TestAccount(testRealm, i));
		}

		final List<User> users = new ArrayList<User>();

		final Random random = new Random(new Date().getTime());
		for (int i = 0; i < 1000; i++) {
			final int operation = random.nextInt(6);
			switch (operation) {
				case 0:
				case 1:
				case 2:
				case 3:
					final Account r = accounts.get(random.nextInt(REALMS_COUNT));
					List<AProperty> newProperties = generateUserProperties(random);
					final User newUser = newUser(r.newUserEntity("user" + String.valueOf(i)), newNeverSyncedUserSyncData(), newProperties);
					users.add(newUser);
					userDao.create(newUser);
					Assert.assertTrue(areEqual(newUser, userDao.read(newUser.getId())));
					Assert.assertTrue(areEqual(newProperties, userDao.readPropertiesById(newUser.getId()), new CollectionEqualizer<AProperty>(null)));
					break;
				case 4:
					if (!users.isEmpty()) {
						int userPosition = random.nextInt(2 * users.size());

						if (userPosition < users.size()) {
							User updatedUser = users.get(userPosition);
							List<AProperty> updatedProperties = generateUserProperties(random);
							updatedUser = newUser(updatedUser.getEntity(), newNeverSyncedUserSyncData(), updatedProperties);
							users.set(userPosition, updatedUser);
							userDao.update(updatedUser);
							Assert.assertTrue(areEqual(updatedProperties, userDao.readPropertiesById(updatedUser.getId()), new CollectionEqualizer<AProperty>(null)));
						} else {
							final Account r2 = accounts.get(random.nextInt(REALMS_COUNT));
							final User newUser2 = newUser(r2.newUserEntity("user" + String.valueOf(i)), newNeverSyncedUserSyncData(), generateUserProperties(random));
							userDao.update(newUser2);
							Assert.assertNull(userDao.read(newUser2.getId()));
							Assert.assertTrue(userDao.readPropertiesById(newUser2.getId()).isEmpty());
						}
					}
					break;
				case 5:
					users.clear();
					userDao.deleteAll();
					break;
			}

			final Collection<String> userIds = userDao.readAllIds();
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
		userDao.deleteAll();
		super.tearDown();
	}
}
