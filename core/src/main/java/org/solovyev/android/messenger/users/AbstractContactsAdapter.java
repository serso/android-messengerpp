package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Filter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.common.JPredicate;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.users.ContactListItem.loadContactListItem;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractContactsAdapter extends BaseListItemAdapter<ContactListItem> {

	@Nonnull
	private static final String TAG = newTag("ContactsAdapter");

	@Nonnull
	private static final String MODE = "mode";

	@Nonnull
	private ContactsDisplayMode mode = Users.DEFAULT_CONTACTS_MODE;

	public AbstractContactsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ContactListItem>());
	}

	public AbstractContactsAdapter(@Nonnull Context context, boolean fastScrollEnabled) {
		super(context, new ArrayList<ContactListItem>(), fastScrollEnabled, true);
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
			case contacts_added:
				// first - filter contacts which can be added
				// then - transform user objects to list items objects
				final List<User> contacts = event.getDataAsUsers();
				if (!Collections.isEmpty(contacts)) {
					addAll(newArrayList(Iterables.transform(Iterables.filter(contacts, new Predicate<User>() {
						@Override
						public boolean apply(@Nullable User contact) {
							assert contact != null;
							return canAddContact(contact);
						}
					}), new Function<User, ContactListItem>() {
						@Override
						public ContactListItem apply(@Nullable User contact) {
							assert contact != null;
							return loadContactListItem(contact);
						}
					})));
				}
				break;
			case changed:
				onContactChanged(event, eventUser);
				break;
			case unread_messages_count_changed:
				onContactChanged(event, eventUser);
				break;
			case contacts_changed:
			case contacts_presence_changed:
				onContactsChanged(event);
				break;
		}
	}

	@Override
	public void restoreState(@Nonnull Bundle savedInstanceState) {
		final Serializable mode = savedInstanceState.getSerializable(MODE);
		if (mode instanceof ContactsDisplayMode) {
			this.mode = (ContactsDisplayMode) mode;
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

	private boolean onContactChanged(@Nonnull UserEvent event, @Nonnull User contact, boolean notifyAndRefilter) {
		boolean changed = false;

		final ContactListItem listItem = findInAllElements(contact);
		if (listItem != null) {
			switch (event.getType()){
				case unread_messages_count_changed:
					changed = listItem.onUnreadMessagesCountChanged(event.getDataAsInteger());
					break;
				case contacts_presence_changed:
					changed = listItem.onContactPresenceChanged(contact);
					break;
				default:
					listItem.onContactChanged(contact);
					changed = true;
					break;
			}

			if (changed) {
				onListItemChanged(contact);

				if (notifyAndRefilter) {
					notifyDataSetChanged();
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

	private void onContactsChanged(@Nonnull UserEvent event) {
		boolean changed = false;

		final List<User> contacts = event.getDataAsUsers();
		for (User contact : contacts) {
			changed |= onContactChanged(event, contact, false);
		}

		if (changed) {
			notifyDataSetChanged();
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
		add(loadContactListItem(contact));
	}

	protected abstract void onListItemChanged(@Nonnull User contact);

	protected abstract boolean canAddContact(@Nonnull User contact);

	public void setMode(@Nonnull ContactsDisplayMode newMode) {
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

		private ContactListItemFilter(@Nullable String query, @Nonnull ContactsDisplayMode mode) {
			filter = new ContactFilter(query, mode);
		}

		@Override
		public boolean apply(@Nullable ContactListItem contactListItem) {
			return contactListItem == null || filter.apply(contactListItem.getContact());
		}
	}

}
