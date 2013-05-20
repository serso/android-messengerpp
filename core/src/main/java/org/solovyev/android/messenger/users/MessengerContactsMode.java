package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 4:51 PM
 */
enum MessengerContactsMode {
	only_online_contacts(R.drawable.mpp_ab_user_offline),
	all_contacts(R.drawable.mpp_ab_user);

	private final int actionBarIconResId;

	MessengerContactsMode(int actionBarIconResId) {
		this.actionBarIconResId = actionBarIconResId;
	}

	int getActionBarIconResId() {
		return actionBarIconResId;
	}
}
