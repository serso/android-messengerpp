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
import org.joda.time.DateTime;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;
import static org.solovyev.android.messenger.users.ContactListItem.newContactListItem;
import static org.solovyev.android.messenger.users.Users.DEFAULT_CONTACTS_MODE;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
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
		if (recentContacts) {
			return RecentContactListItemComparator.getInstance();
		} else {
			return super.getComparator();
		}
	}

	public void onEvent(@Nonnull ChatEvent event) {
		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case last_message_changed:
				if (recentContacts && eventChat.isPrivate()) {
					final Message lastMessage = event.getDataAsMessage();
					final DateTime lastMessageDate = lastMessage.getSendDate();

					final Entity participant = eventChat.getSecondUser();
					final ContactListItem listItem = findInAllElements(newEmptyUser(participant));

					boolean changed = false;
					if (listItem != null) {
						listItem.onLastMessageDataChanged(lastMessageDate);
						changed = true;
					} else {
						final int count = getCount();
						if (count > 0) {
							// some contacts exist => need to check if we can be shown (if we are later than the
							// last shown contact)
							final ContactListItem lastItem = getItem(count - 1);
							if (compareSendDatesLatestFirst(lastItem.getData().getLastMessageDate(), lastMessageDate) <= 0) {
								add(newContactListItem(UiContact.loadRecentUiContact(App.getUserService().getUserById(participant), lastMessageDate)));
								changed = true;
							}
						} else {
							// no contacts, just add
							add(newContactListItem(UiContact.loadRecentUiContact(App.getUserService().getUserById(participant), lastMessageDate)));
							changed = true;
						}
					}

					if (changed) {
						notifyDataSetChanged();
					}

				}
				break;
		}
	}
}
