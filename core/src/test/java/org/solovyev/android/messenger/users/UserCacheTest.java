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

package org.solovyev.android.messenger.users;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class UserCacheTest {

	@Test
	public void testShouldAddUserIfNotExistsOnUserChangedEvent() throws Exception {
		final UserCache cache = new UserCache();
		final User expected = newEmptyUser("test:test");
		assertNull(cache.get(expected.getEntity()));

		cache.onEvent(UserEventType.changed.newEvent(expected));

		assertSame(expected, cache.get(expected.getEntity()));
	}

	@Test
	public void testShouldUpdateUserIfExistsOnUserChangedEvent() throws Exception {
		final UserCache cache = new UserCache();
		final User user = newEmptyUser("test:test");
		cache.put(user);

		final User expected = user.clone();

		cache.onEvent(UserEventType.changed.newEvent(expected));

		assertSame(expected, cache.get(expected.getEntity()));
	}

	@Test
	public void testShouldUpdateUserIfPresenceChanged() throws Exception {
		final UserCache cache = new UserCache();
		final User user = newEmptyUser("test:test").cloneWithNewStatus(false);
		cache.put(user);

		final User expected = user.cloneWithNewStatus(true);

		cache.onEvent(UserEventType.contacts_presence_changed.newEvent(expected, Arrays.asList(expected)));

		assertSame(expected, cache.get(expected.getEntity()));

	}
}
