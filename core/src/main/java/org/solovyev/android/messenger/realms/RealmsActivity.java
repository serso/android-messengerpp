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

package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import roboguice.RoboGuice;
import roboguice.event.Observes;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment.newCreateAccountConfigurationFragmentDef;

public class RealmsActivity extends BaseFragmentActivity {

	public static void start(@Nonnull Activity activity) {
		final Intent intent = new Intent();
		intent.setClass(activity, RealmsActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		EventsListener.listenTo(this);

		if (savedInstanceState == null) {
			// first time
			fragmentManager.setMainFragment(PrimaryFragment.realms);
		}

		initFragments();
	}

	public static class EventsListener {

		@Nonnull
		private BaseFragmentActivity activity;

		public EventsListener(@Nonnull BaseFragmentActivity activity) {
			this.activity = activity;
		}

		public static void listenTo(@Nonnull BaseFragmentActivity activity) {
			final EventsListener listener = new EventsListener(activity);
			RoboGuice.getInjector(activity).injectMembers(listener);
		}

		public void onAccountSaved(@Observes @Nonnull AccountUiEvent.Saved event) {
			activity.finish();
		}

		public void onRealmClicked(@Observes @Nonnull RealmUiEvent.Clicked event) {
			final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
			if (activity.isDualPane()) {
				mpfm.setSecondFragment(newCreateAccountConfigurationFragmentDef(activity, event.realm, false));
				if (activity.isTriplePane()) {
					mpfm.emptifyThirdFragment();
				}
			} else {
				mpfm.setMainFragment(newCreateAccountConfigurationFragmentDef(activity, event.realm, true));
			}
		}
	}
}
