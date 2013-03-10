package org.solovyev.android.messenger.realms;

import junit.framework.Assert;
import org.junit.Test;

public class RealmEntityImplTest {

    @Test
    public void testFromUserId() throws Exception {
        try {
            RealmEntityImpl.fromEntityId("test");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            RealmEntityImpl.fromEntityId("test:");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            RealmEntityImpl.fromEntityId(":test");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        RealmEntity actual = RealmEntityImpl.fromEntityId("1:test");
        Assert.assertEquals("1", actual.getRealmId());
        Assert.assertEquals("test", actual.getRealmEntityId());

        actual = RealmEntityImpl.fromEntityId("test:1");
        Assert.assertEquals("test", actual.getRealmId());
        Assert.assertEquals("1", actual.getRealmEntityId());

        actual = RealmEntityImpl.fromEntityId("1:2:3");
        Assert.assertEquals("1", actual.getRealmId());
        Assert.assertEquals("2:3", actual.getRealmEntityId());

    }
}
