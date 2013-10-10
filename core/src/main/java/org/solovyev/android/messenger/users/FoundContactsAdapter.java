package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.Filter;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

import static org.solovyev.android.messenger.users.Users.DEFAULT_CONTACTS_MODE;
import static org.solovyev.common.Objects.areEqual;

public class FoundContactsAdapter extends AbstractContactsAdapter {

	@Nonnull
	private ContactFilter contactFilter = new ContactFilter(null, DEFAULT_CONTACTS_MODE);

	private boolean recentContacts;

	public FoundContactsAdapter(@Nonnull Context context, boolean recentContacts) {
		super(context, false);
		this.recentContacts = recentContacts;
	}

	@Override
	protected void onListItemChanged(@Nonnull User contact) {
	}

	public void setRecentContacts(boolean recentContacts) {
		this.recentContacts = recentContacts;
	}

	@Override
	protected boolean canAddContact(@Nonnull User contact) {
		final CharSequence filterText = getFilterText();
		final String prefix = filterText == null ? null : filterText.toString();
		if(!areEqual(contactFilter.getPrefix(), prefix)) {
			contactFilter = new ContactFilter(prefix, DEFAULT_CONTACTS_MODE);
		}
		return contactFilter.apply(contact);
	}

	@Nonnull
	@Override
	protected Filter createFilter() {
		return new AdapterFilter<ContactListItem>(new AdapterHelper()) {
			@Override
			protected JPredicate<ContactListItem> getFilter(@Nullable CharSequence prefix) {
				return new JPredicate<ContactListItem>() {
					@Override
					public boolean apply(@Nullable ContactListItem contactListItem) {
						return true;
					}
				};
			}
		};
	}

	@Nullable
	@Override
	protected Comparator<? super ContactListItem> getComparator() {
		return recentContacts ? null : super.getComparator();
	}
}
