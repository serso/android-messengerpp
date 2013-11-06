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

import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.application;
import static org.solovyev.android.messenger.users.UserEventType.contacts_presence_changed;
import static org.solovyev.common.Objects.areEqual;

public class FoundContactsAdapterTest extends DefaultMessengerTest {

	@Test
	public void testShouldUpdateUserPresence() throws Exception {
		FoundContactsAdapter adapter = new FoundContactsAdapter(application, true);

		final AccountData ad = getAccountData1();
		final List<User> contacts = ad.getContacts();
		final Map<User, ContactListItem> contactListItems = new HashMap<User, ContactListItem>();
		for (User contact : contacts) {
			contactListItems.put(contact, ContactListItem.loadContactListItem(contact));
		}
		adapter.addAll(contactListItems.values());

		final List<User> expectedContacts = new ArrayList<User>(contacts.size());
		for (User contact : contacts) {
			expectedContacts.add(contact.cloneWithNewStatus(!contact.isOnline()));
		}
		adapter.onEvent(contacts_presence_changed.newEvent(ad.getAccount().getUser(), expectedContacts));

		for (User expectedContact : expectedContacts) {
			final ContactListItem li = contactListItems.get(expectedContact);
			assertTrue(areEqual(expectedContact, li.getContact(), new UserSameEqualizer()));
		}

	}
}
