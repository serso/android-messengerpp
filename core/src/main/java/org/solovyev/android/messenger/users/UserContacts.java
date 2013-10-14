package org.solovyev.android.messenger.users;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAwareRemovedUpdater;
import org.solovyev.common.collections.multimap.ObjectAddedUpdater;
import org.solovyev.common.collections.multimap.ObjectChangedMapUpdater;
import org.solovyev.common.collections.multimap.ObjectsAddedUpdater;
import org.solovyev.common.collections.multimap.ThreadSafeMultimap;
import org.solovyev.common.collections.multimap.WholeListUpdater;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
		this.contacts.update(user, new WholeListUpdater<User>(contacts));
	}

	public void onEvent(@Nonnull UserEvent event) {
		final User user = event.getUser();

		switch (event.getType()) {
			case changed:
				this.contacts.update(new ObjectChangedMapUpdater<Entity, User>(user));
				break;
			case contact_added:
				// contact added => need to add to list of cached contacts
				final User contact = event.getDataAsUser();
				this.contacts.update(user.getEntity(), new ObjectAddedUpdater<User>(contact));
				break;
			case contact_added_batch:
				// contacts added => need to add to list of cached contacts
				final List<User> contacts = event.getDataAsUsers();
				this.contacts.update(user.getEntity(), new ObjectsAddedUpdater<User>(contacts));
				break;
			case contact_removed:
				// contact removed => try to remove from cached contacts
				final String removedContactId = event.getDataAsUserId();
				this.contacts.update(user.getEntity(), new EntityAwareRemovedUpdater<User>(removedContactId));
				break;
			case contacts_presence_changed:
				this.contacts.update(user.getEntity(), new UserListContactStatusUpdater(event.getDataAsUsers()));
				break;
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
