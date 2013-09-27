package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.Filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.AdapterFilter;
import org.solovyev.common.JPredicate;
import org.solovyev.common.Objects;

public class FoundContactsAdapter extends AbstractContactsAdapter {

	@Nonnull
	private ContactFilter contactFilter = new ContactFilter(null, Users.DEFAULT_CONTACTS_MODE);

	public FoundContactsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected void onListItemChanged(@Nonnull User contact) {

	}

	@Override
	protected boolean canAddContact(@Nonnull User contact) {
		final CharSequence filterText = getFilterText();
		final String prefix = filterText == null ? null : filterText.toString();
		if(!Objects.areEqual(contactFilter.getPrefix(), prefix)) {
			contactFilter = new ContactFilter(prefix, Users.DEFAULT_CONTACTS_MODE);
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
}
