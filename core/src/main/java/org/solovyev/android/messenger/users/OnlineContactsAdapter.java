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

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountService;

public class OnlineContactsAdapter extends AbstractContactsAdapter {

	public OnlineContactsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	public void onEvent(@Nonnull UserEvent event) {
		super.onEvent(event);

		switch (event.getType()) {
			case contacts_presence_changed:
				for (User contact : event.getDataAsUsers()) {
					if(contact.isOnline()) {
						tryAddOnlineContact(contact);
					}
				}
				break;
		}
	}

	private void tryAddOnlineContact(@Nonnull User contact) {
		final ContactListItem listItem = findInAllElements(contact);
		if (listItem == null) {
			addListItem(contact);
		}
	}

	@Override
	protected void onListItemChanged(@Nonnull User contact) {
		if (!contact.isOnline()) {
			removeListItem(contact);
		}
	}

	@Override
	protected boolean canAddContact(@Nonnull User contact) {
		return contact.isOnline();
	}
}
