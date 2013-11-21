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

import android.util.Log;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.common.text.Strings.isEmpty;

public class ContactsFragment extends BaseContactsFragment {

	@Nonnull
	@Override
	protected BaseListItemAdapter<ContactListItem> createAdapter() {
		Log.d(tag, "Creating adapter, filter text: " + getFilterText());
		return new ContactsAdapter(getActivity(), isRecentContacts());
	}

	private boolean isRecentContacts() {
		return isEmpty(getFilterText());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiContact>> createAsyncLoader(@Nonnull BaseListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		final CharSequence filterText = getFilterText();
		Log.d(tag, "Creating loader, filter text: " + filterText);
		if (!isEmpty(filterText)) {
			((ContactsAdapter) adapter).setRecentContacts(false);
			((BaseContactsAdapter) adapter).setQuery(filterText.toString());
			return new ContactsAsyncLoader(getActivity(), adapter, onPostExecute, filterText.toString(), getMaxSize());
		} else {
			((ContactsAdapter) adapter).setRecentContacts(true);
			((BaseContactsAdapter) adapter).setQuery(null);
			return new RecentContactsAsyncLoader(getActivity(), adapter, onPostExecute, getMaxSize());
		}
	}
}
