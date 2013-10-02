package org.solovyev.android.messenger.realms;

import junit.framework.Assert;

import org.junit.Test;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;

public class AccountEntityImplTest {

	@Test
	public void testFromUserId() throws Exception {
		try {
			Entities.newEntityFromEntityId("test");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			Entities.newEntityFromEntityId("test:");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			Entities.newEntityFromEntityId(":test");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}

		Entity actual = Entities.newEntityFromEntityId("1:test");
		Assert.assertEquals("1", actual.getAccountId());
		Assert.assertEquals("test", actual.getAccountEntityId());

		actual = Entities.newEntityFromEntityId("test:1");
		Assert.assertEquals("test", actual.getAccountId());
		Assert.assertEquals("1", actual.getAccountEntityId());

		actual = Entities.newEntityFromEntityId("1:2:3");
		Assert.assertEquals("1", actual.getAccountId());
		Assert.assertEquals("2:3", actual.getAccountEntityId());

	}
}
