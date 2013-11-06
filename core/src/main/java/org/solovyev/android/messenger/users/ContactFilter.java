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
import org.solovyev.android.list.PrefixFilter;
import org.solovyev.common.JPredicate;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.newTag;

final class ContactFilter implements JPredicate<User> {

	private final String TAG = newTag("ContactFilter");

	@Nullable
	private final String prefix;

	@Nullable
	private final PrefixFilter<String> prefixFilter;

	@Nonnull
	private final ContactsDisplayMode mode;

	public ContactFilter(@Nullable String prefix, @Nonnull ContactsDisplayMode mode) {
		this.prefix = prefix;
		this.mode = mode;
		if (!Strings.isEmpty(prefix)) {
			assert prefix != null;
			prefixFilter = new PrefixFilter<String>(prefix.toLowerCase());
		} else {
			prefixFilter = null;
		}
	}

	@Override
	public boolean apply(@Nullable User contact) {
		if (contact != null) {
			boolean shown = true;
			if (mode == ContactsDisplayMode.all_contacts) {
				shown = true;
			} else if (mode == ContactsDisplayMode.only_online_contacts) {
				shown = contact.isOnline();
			}

			if (shown) {
				if (prefixFilter != null) {
					shown = prefixFilter.apply(contact.getDisplayName().toString());
					if (!shown) {
						Log.d(TAG, contact.getDisplayName() + " is filtered due to filter " + prefix);
					}
				}
			} else {
				Log.d(TAG, contact.getDisplayName() + " is filtered due to mode " + mode);
			}

			return shown;
		}

		return false;
	}

	@Nullable
	public String getPrefix() {
		return prefix;
	}
}
