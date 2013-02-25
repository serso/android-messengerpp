package org.solovyev.android.messenger.realms;

import junit.framework.Assert;
import org.junit.Test;

public class RealmUserImplTest {

    @Test
    public void testFromUserId() throws Exception {
        try {
            RealmUserImpl.fromUserId("test");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            RealmUserImpl.fromUserId("test_");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            RealmUserImpl.fromUserId("_test");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        RealmEntity actual = RealmUserImpl.fromUserId("1_test");
        Assert.assertEquals("1", actual.getRealmId());
        Assert.assertEquals("test", actual.getRealmEntityId());

        actual = RealmUserImpl.fromUserId("test_1");
        Assert.assertEquals("test", actual.getRealmId());
        Assert.assertEquals("1", actual.getRealmEntityId());

        actual = RealmUserImpl.fromUserId("1_2_3");
        Assert.assertEquals("1", actual.getRealmId());
        Assert.assertEquals("2_3", actual.getRealmEntityId());

    }
}
