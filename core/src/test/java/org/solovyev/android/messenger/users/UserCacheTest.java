package org.solovyev.android.messenger.users;

import java.util.Arrays;

import org.junit.Test;

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
