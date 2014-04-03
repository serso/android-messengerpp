/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms;

import org.junit.Assert;
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
