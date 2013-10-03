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
