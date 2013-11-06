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

import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			case contacts_changed:
			case contacts_presence_changed:
				put(event.getDataAsUsers());
				break;
		}
	}
}
