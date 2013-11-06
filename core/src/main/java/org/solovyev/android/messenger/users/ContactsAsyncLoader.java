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
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.users.ContactListItem.newContactListItem;
import static org.solovyev.android.messenger.users.UiContact.loadUiContact;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:12 PM
 */
final class ContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	ContactsAsyncLoader(@Nonnull Context context,
						@Nonnull ListAdapter<ContactListItem> adapter,
						@Nullable Runnable onPostExecute) {
		super(context, adapter, onPostExecute);
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final AccountService accountService = App.getAccountService();
		final UserService userService = App.getUserService();

		for (User user : accountService.getEnabledAccountUsers()) {
			for (User contact : userService.getUserContacts(user.getEntity())) {
				result.add(loadUiContact(contact));
			}
		}

		return result;
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return newContactListItem(uiContact);
	}
}
