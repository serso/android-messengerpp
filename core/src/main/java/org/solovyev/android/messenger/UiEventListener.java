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

package org.solovyev.android.messenger;

import android.app.Activity;
import org.solovyev.android.messenger.about.AboutActivity;
import org.solovyev.android.messenger.accounts.AccountsActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.preferences.PreferencesActivity;
import org.solovyev.android.messenger.realms.RealmsActivity;
import org.solovyev.android.messenger.users.ContactsActivity;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

public class UiEventListener implements EventListener<UiEvent> {

	@Nonnull
	private final Activity activity;

	public UiEventListener(@Nonnull Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull UiEvent event) {
		switch (event.getType()) {
			case show_realms:
				RealmsActivity.start(activity);
				break;
			case new_chat:
				App.showToast(R.string.mpp_pick_contact_to_start_chat);
				ContactsActivity.start(activity);
				break;
			case show_settings:
				PreferencesActivity.start(activity);
				break;
			case show_about:
				AboutActivity.start(activity);
				break;
			case show_accounts:
				AccountsActivity.start(activity);
				break;
			case show_contacts:
				ContactsActivity.start(activity);
				break;
			case exit:
				App.exit(activity);
				break;
		}
	}
}
