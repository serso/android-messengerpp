package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Filter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.common.JPredicate;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.users.UserEventType.contacts_presence_changed;
import static org.solovyev.android.messenger.users.UserEventType.unread_messages_count_changed;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractContactsAdapter extends MessengerListItemAdapter<ContactListItem> {

	@Nonnull
	private static final String TAG = newTag("ContactsAdapter");

	@Nonnull
	private static final String MODE = "mode";

	@Nonnull
	private MessengerContactsMode mode = Users.DEFAULT_CONTACTS_MODE;

	public AbstractContactsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ContactListItem>());
	}

	@Override
	public void onEvent(@Nonnull UserEvent event) {
		super.onEvent(event);

		Log.i(TAG, "Event received: " + event.getType());
		Log.i(TAG, "Shown contacts before event: " + getCount());

		final UserEventType type = event.getType();
		final User eventUser = event.getUser();

		switch (type) {
			case contact_removed:
				final String contactId = event.getDataAsUserId();
				removeListItem(contactId);
				break;
			case contact_added:
				final User contact = event.getDataAsUser();
				if (canAddContact(contact)) {
					addListItem(contact);
				}
				break;
			case contact_added_batch:
				// first - filter contacts which can be added
				// then - transform user objects to list items objects
				final List<User> contacts = event.getDataAsUsers();
				if (!Collections.isEmpty(contacts)) {
					addAll(Lists.newArrayList(Iterables.transform(Iterables.filter(contacts, new Predicate<User>() {
						@Override
						public boolean apply(@Nullable User contact) {
							assert contact != null;
							return canAddContact(contact);
						}
					}), new Function<User, ContactListItem>() {
						@Override
						public ContactListItem apply(@Nullable User contact) {
							assert contact != null;
							return ContactListItem.newInstance(contact);
						}
					})));
				}
				break;
			case changed:
				// change of user is frequent operation - to avoid heavy work on main thread let's
				// not change user in list (user still be persisted and changes will be shown later)
				// onContactChanged(event, eventUser);
				break;
			case unread_messages_count_changed:
				onContactChanged(event, eventUser);
				break;
			case contacts_presence_changed:
				onContactsPresenceChanged(event);
				break;
		}
	}

	@Override
	public void restoreState(@Nonnull Bundle savedInstanceState) {
		final Serializable mode = savedInstanceState.getSerializable(MODE);
		if (mode instanceof MessengerContactsMode) {
			this.mode = (MessengerContactsMode) mode;
		}
	}

	@Override
	public void saveState(@Nonnull Bundle outState) {
		super.saveState(outState);

		outState.putSerializable(MODE, mode);
	}

	private void onContactChanged(@Nonnull UserEvent event, @Nonnull User contact) {
		onContactChanged(event, contact, true);
	}

	private boolean onContactChanged(@Nonnull UserEvent event, @Nonnull User contact, boolean refilter) {
		boolean changed = false;

		final ContactListItem listItem = findInAllElements(contact);
		if (listItem != null) {
			changed = true;
			if(event.getType() == unread_messages_count_changed) {
				listItem.onUnreadMessagesCountChanged(event.getDataAsInteger());
			} else if(event.getType() == contacts_presence_changed) {
				if(contact.isOnline() != listItem.getContact().isOnline()) {
					listItem.onContactChanged(contact);
				} else {
					changed = false;
				}
			} else {
				listItem.onContactChanged(contact);
			}

			if (changed) {
				onListItemChanged(contact);

				if (refilter) {
					refilter();
				}
			}
		}

		return changed;
	}

	public void refilter() {
		this.getFilter().filter(getFilterText(), new Filter.FilterListener() {
			@Override
			public void onFilterComplete(int count) {
				Log.i(TAG, "Shown contacts after filter: " + count);
			}
		});
	}

	private void onContactsPresenceChanged(@Nonnull UserEvent event) {
		boolean changed = false;

		final List<User> contacts = event.getDataAsUsers();
		for (User contact : contacts) {
			changed |= onContactChanged(event, contact, false);
		}

		if (changed) {
			refilter();
		}
	}

	@Nullable
	protected ContactListItem findInAllElements(@Nonnull User contact) {
		return Iterables.find(getAllElements(), Predicates.<ContactListItem>equalTo(ContactListItem.newEmpty(contact)), null);
	}


	protected void removeListItem(@Nonnull String contactId) {
		final User contact = Users.newEmptyUser(contactId);
		removeListItem(contact);
	}

	protected void removeListItem(@Nonnull User contact) {
		remove(ContactListItem.newEmpty(contact));
	}

	protected void addListItem(@Nonnull User contact) {
		add(ContactListItem.newInstance(contact));
	}

	protected abstract void onListItemChanged(@Nonnull User contact);

	protected abstract boolean canAddContact(@Nonnull User contact);

	public void setMode(@Nonnull MessengerContactsMode newMode) {
		boolean changed = this.mode != newMode;
		this.mode = newMode;
		if (changed) {
			refilter();
		}
	}

	@Nonnull
	@Override
	protected Filter createFilter() {
		return new ContactsFilter(new AdapterHelper());
	}

	private class ContactsFilter extends AdapterFilter<ContactListItem> {

		@Nonnull
		private ContactListItemFilter emptyPrefixFilter = new ContactListItemFilter(null, mode);

		private ContactsFilter(@Nonnull Helper<ContactListItem> helper) {
			super(helper);
		}

		@Override
		protected boolean doFilterOnEmptyString() {
			return true;
		}

		@Override
		protected JPredicate<ContactListItem> getFilter(@Nullable final CharSequence prefix) {
			if (Strings.isEmpty(prefix)) {
				return emptyPrefixFilter;
			} else {
				assert prefix != null;
				return new ContactListItemFilter(prefix.toString().toLowerCase(), mode);
			}
		}
	}

	private static final class ContactListItemFilter implements JPredicate<ContactListItem> {

		private final JPredicate<User> filter;

		private ContactListItemFilter(@Nullable String query, @Nonnull MessengerContactsMode mode) {
			filter = new ContactFilter(query, mode);
		}

		@Override
		public boolean apply(@Nullable ContactListItem contactListItem) {
			return contactListItem == null || filter.apply(contactListItem.getContact());
		}
	}

}
