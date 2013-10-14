package org.solovyev.android.messenger.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.entities.Entity;

@ThreadSafe
class UserCache {

	// key: user entity, value: user object
	@GuardedBy("users")
	@Nonnull
	private final Map<Entity, User> users = new HashMap<Entity, User>();

	@Nullable
	public User get(@Nonnull Entity key) {
		synchronized (users) {
			return users.get(key);
		}
	}

	public void put(@Nonnull User user) {
		synchronized (users) {
			users.put(user.getEntity(), user);
		}
	}

	private void put(@Nonnull List<User> users) {
		synchronized (this.users) {
			for (User user : users) {
				this.users.put(user.getEntity(), user);
			}
		}
	}

	public void onEvent(@Nonnull UserEvent event) {
		final User user = event.getUser();
		switch (event.getType()) {
			case changed:
				put(user);
				break;
			case contacts_presence_changed:
				put(event.getDataAsUsers());
				break;
		}
	}
}
