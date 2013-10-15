package org.solovyev.android.messenger.users;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAwareRemovedUpdater;
import org.solovyev.common.collections.multimap.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static org.solovyev.common.collections.multimap.ThreadSafeMultimap.newThreadSafeMultimap;

@ThreadSafe
class UserContacts {

	// key: user entity, value: list of user contacts
	@Nonnull
	private final ThreadSafeMultimap<Entity, User> contacts = newThreadSafeMultimap();

	@Nonnull
	public List<User> getContacts(@Nonnull Entity user) {
		return contacts.get(user);
	}

	public void update(@Nonnull Entity user, @Nonnull List<User> contacts) {
		if (!contacts.isEmpty()) {
			calculateDisplayNames(contacts);
			this.contacts.update(user, new WholeListUpdater<User>(contacts));
		} else {
			this.contacts.remove(user);
		}
	}

	public void onEvent(@Nonnull UserEvent event) {
		final User user = event.getUser();

		switch (event.getType()) {
			case changed:
				this.contacts.update(new ObjectsChangedMapUpdater<Entity, User>(user));
				break;
			case contacts_added:
				// contacts added => need to add to list of cached contacts
				final List<User> contacts = event.getDataAsUsers();
				calculateDisplayNames(contacts);
				this.contacts.update(user.getEntity(), new ObjectsAddedUpdater<User>(contacts));
				break;
			case contact_removed:
				// contact removed => try to remove from cached contacts
				final String removedContactId = event.getDataAsUserId();
				this.contacts.update(user.getEntity(), new EntityAwareRemovedUpdater<User>(removedContactId));
				break;
			case contacts_changed:
				this.contacts.update(new ObjectsChangedMapUpdater<Entity, User>(event.getDataAsUsers()));
				break;
			case contacts_presence_changed:
				this.contacts.update(user.getEntity(), new UserListContactStatusUpdater(event.getDataAsUsers()));
				break;
		}
	}

	private void calculateDisplayNames(@Nonnull List<User> contacts) {
		for (User contact : contacts) {
			// update cached value
			contact.getDisplayName();
		}
	}

	private static class UserListContactStatusUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final List<User> contacts;

		public UserListContactStatusUpdater(@Nonnull List<User> contacts) {
			this.contacts = contacts;
		}

		@Nullable
		@Override
		public List<User> update(@Nonnull List<User> values) {
			if (contacts.size() == 1) {
				final User contact = contacts.get(0);

				final int index = Iterables.indexOf(values, new Predicate<User>() {
					@Override
					public boolean apply(@Nullable User user) {
						return contact.equals(user);
					}
				});

				if (index >= 0) {
					final List<User> result = ThreadSafeMultimap.copy(values);
					result.set(index, result.get(index).cloneWithNewStatus(contact.isOnline()));
					return result;
				} else {
					return null;
				}
			} else {
				final List<User> result = ThreadSafeMultimap.copy(values);

				for (int i = 0; i < result.size(); i++) {
					final User user = result.get(i);
					final User contact = find(contacts, new Predicate<User>() {
						@Override
						public boolean apply(@Nullable User contact) {
							return user.equals(contact);
						}
					}, null);

					if(contact != null) {
						result.set(i, user.cloneWithNewStatus(contact.isOnline()));
					}
				}

				return result;
			}
		}
	}

}
