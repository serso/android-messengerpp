/*
 * Copyright 2014 serso aka se.solovyev
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

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.Comparator;

import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;

final class RecentContactListItemComparator implements Comparator<ContactListItem> {

	@Nonnull
	private static final RecentContactListItemComparator instance = new RecentContactListItemComparator();

	RecentContactListItemComparator() {
	}

	@Nonnull
	static Comparator<ContactListItem> getInstance() {
		return instance;
	}

	@Override
	public int compare(@Nonnull ContactListItem lhs, @Nonnull ContactListItem rhs) {
		final DateTime lm = lhs.getData().getLastMessageDate();
		final DateTime rm = rhs.getData().getLastMessageDate();
		return compareSendDatesLatestFirst(lm, rm);
	}
}
