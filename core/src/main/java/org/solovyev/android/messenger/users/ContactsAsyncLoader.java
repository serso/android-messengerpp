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
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseAsyncLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.users.ContactListItem.newContactListItem;
import static org.solovyev.android.messenger.users.UserService.ContactsSearchStrategy.alphabetically;

final class ContactsAsyncLoader extends BaseAsyncLoader<UiContact, ContactListItem> {

	@Nullable
	private String query;

	private final int maxCount;

	ContactsAsyncLoader(@Nonnull Context context,
						@Nonnull ListAdapter<ContactListItem> adapter,
						@Nullable Runnable onPostExecute,
						@Nullable String query,
						int maxCount) {
		super(context, adapter, onPostExecute);
		this.query = query;
		this.maxCount = maxCount;
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		return App.getUserService().findContacts(query, maxCount, alphabetically);
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return newContactListItem(uiContact);
	}

	@Override
	public String toString() {
		return "ContactsAsyncLoader{" +
				"query='" + query + '\'' +
				", maxCount=" + maxCount +
				'}';
	}
}
