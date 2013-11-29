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

import com.actionbarsherlock.app.ActionBar;
import org.solovyev.android.messenger.about.AboutActivity;
import org.solovyev.android.messenger.accounts.AccountsActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.preferences.PreferencesActivity;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.realms;
import static org.solovyev.android.messenger.users.Users.CONTACTS_FRAGMENT_TAG;

public class UiEventListener implements EventListener<UiEvent> {

	@Nonnull
	private final BaseFragmentActivity activity;

	public UiEventListener(@Nonnull BaseFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull UiEvent event) {
		switch (event.getType()) {
			case show_realms:
				onShowRealmsEvent();
				break;
			case new_message:
				onNewMessageEvent();
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
			case exit:
				App.exit(activity);
				break;
			case new_contact:
				onNewContactEvent();
				break;
		}
	}

	private void onNewContactEvent() {
		NewContactActivity.start(activity);
	}

	private void onNewMessageEvent() {
		showToast(R.string.mpp_pick_contact_to_start_chat);
		final ActionBar.Tab tab = activity.findTabByTag(CONTACTS_FRAGMENT_TAG);
		if (tab != null) {
			tab.select();
		}
	}

	private void onShowRealmsEvent() {
		activity.getMultiPaneFragmentManager().setMainFragment(realms);
	}
}