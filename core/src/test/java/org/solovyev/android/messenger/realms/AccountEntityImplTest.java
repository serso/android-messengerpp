package org.solovyev.android.messenger.realms;

import junit.framework.Assert;
import org.junit.Test;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;

public class AccountEntityImplTest {

	@Test
	public void testFromUserId() throws Exception {
		try {
			EntityImpl.fromEntityId("test");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			EntityImpl.fromEntityId("test:");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			EntityImpl.fromEntityId(":test");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		Entity actual = EntityImpl.fromEntityId("1:test");
		Assert.assertEquals("1", actual.getAccountId());
		Assert.assertEquals("test", actual.getAccountEntityId());

		actual = EntityImpl.fromEntityId("test:1");
		Assert.assertEquals("test", actual.getAccountId());
		Assert.assertEquals("1", actual.getAccountEntityId());

		actual = EntityImpl.fromEntityId("1:2:3");
		Assert.assertEquals("1", actual.getAccountId());
		Assert.assertEquals("2:3", actual.getAccountEntityId());

	}
}
