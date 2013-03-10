package org.solovyev.android.messenger.users;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.realms.TestRealmDef;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.ListEqualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:50 PM
 */
public class SqliteUserDaoTest extends AbstractMessengerTestCase {

    @Inject
    private UserDao userDao;

    @Inject
    private TestRealmDef testRealmDef;

    @Inject
    private TestRealm testRealm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userDao.deleteAllUsers();
    }

    public void testUserOperations() throws Exception {
        // INSERT

        List<AProperty> expectedProperties = new ArrayList<AProperty>();
        expectedProperties.add(APropertyImpl.newInstance("prop_1", "prop_1_value"));
        expectedProperties.add(APropertyImpl.newInstance("prop_2", "prop_2_value"));
        expectedProperties.add(APropertyImpl.newInstance("prop_3", "prop_3_value"));

        final RealmEntity realmUser = testRealm.newRealmEntity("2");

        User expected = Users.newUser(realmUser, UserSyncDataImpl.newNeverSyncedInstance(), expectedProperties);

        userDao.insertUser(expected);
        userDao.insertUser(expected);

        User actual = userDao.loadUserById("test~1:2");

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(realmUser.getRealmId(), actual.getRealmEntity().getRealmId());
        Assert.assertEquals("2", actual.getRealmEntity().getRealmEntityId());
        Assert.assertEquals("test~1:2", actual.getRealmEntity().getEntityId());
        Assert.assertTrue(Objects.areEqual(expectedProperties, actual.getProperties(), new CollectionEqualizer<AProperty>(null)));
        Assert.assertEquals("prop_1_value", actual.getPropertyValueByName("prop_1"));

        User actual2 = userDao.loadUserById("test~1:2");
        Assert.assertEquals(expected, actual2);

        User actual3 = userDao.loadUserById("test_01");
        Assert.assertNull(actual3);

        Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2"), ListEqualizer.<String>newWithNaturalEquals(false)));

        final RealmEntity realmUser2 = testRealm.newRealmEntity("3");

        expected = Users.newUser(realmUser2, UserSyncDataImpl.newInstance(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
        userDao.insertUser(expected);
        Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

        // UPDATE
        expectedProperties = new ArrayList<AProperty>(expectedProperties);
        expectedProperties.remove(0);
        expectedProperties.add(APropertyImpl.newInstance("prop_4", "prop_4_value"));

        expected = Users.newUser(realmUser, UserSyncDataImpl.newInstance(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
        userDao.updateUser(expected);
        actual = userDao.loadUserById("test~1:2");

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
        Assert.assertTrue(Objects.areEqual(expectedProperties, actual.getProperties(), new CollectionEqualizer<AProperty>(null)));

        Assert.assertTrue(Objects.areEqual(userDao.loadUserIds(), Arrays.asList("test~1:2", "test~1:3"), ListEqualizer.<String>newWithNaturalEquals(false)));

        expected = Users.newUser(TestRealmDef.REALM_ID, "test_01dsfsdfsf", UserSyncDataImpl.newInstance(DateTime.now(), DateTime.now(), DateTime.now(), DateTime.now()), expectedProperties);
        userDao.updateUser(expected);
    }

    @Override
    public void tearDown() throws Exception {
        userDao.deleteAllUsers();
        super.tearDown();
    }
}
