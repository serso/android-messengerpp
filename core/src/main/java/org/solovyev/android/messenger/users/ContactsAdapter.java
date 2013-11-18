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

import android.content.Context;
import android.widget.Filter;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

import static org.solovyev.android.messenger.users.Users.DEFAULT_CONTACTS_MODE;
import static org.solovyev.common.Objects.areEqual;

public class ContactsAdapter extends BaseContactsAdapter {

	@Nonnull
	private ContactFilter filter = new ContactFilter(null, DEFAULT_CONTACTS_MODE);

	private boolean recentContacts;

	public ContactsAdapter(@Nonnull Context context, boolean recentContacts) {
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
		final String query = getQuery();
		if (!areEqual(filter.getPrefix(), query)) {
			filter = new ContactFilter(query, DEFAULT_CONTACTS_MODE);
		}
		return filter.apply(contact);
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
