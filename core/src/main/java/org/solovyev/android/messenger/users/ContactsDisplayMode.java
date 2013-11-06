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

import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 4:51 PM
 */
enum ContactsDisplayMode {
	only_online_contacts(R.drawable.mpp_ab_user_offline),
	all_contacts(R.drawable.mpp_ab_user);

	private final int actionBarIconResId;

	ContactsDisplayMode(int actionBarIconResId) {
		this.actionBarIconResId = actionBarIconResId;
	}

	int getActionBarIconResId() {
		return actionBarIconResId;
	}
}
