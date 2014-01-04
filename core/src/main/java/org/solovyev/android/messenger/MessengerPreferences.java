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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.IntegerPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;

import javax.annotation.Nonnull;

import static org.solovyev.android.Android.getAppVersionCode;
import static org.solovyev.android.messenger.App.getPreferences;

public final class MessengerPreferences {

	private static final int NO_VERSION = -1;

	public static void setDefaultValues(@Nonnull Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		Gui.theme.tryPutDefault(preferences);
		Gui.Chat.Message.showIcon.tryPutDefault(preferences);
		startOnBoot.tryPutDefault(preferences);
        isOngoingNotificationEnabled.tryPutDefault(preferences);

		final int versionCode = getAppVersionCode(context);
		final Integer version = MessengerPreferences.version.getPreference(preferences);
		if (version > NO_VERSION) {
			if (version < versionCode) {
				// application update
				MessengerPreferences.previousVersion.putPreference(preferences, version);
				MessengerPreferences.version.putPreference(preferences, versionCode);
			}
		} else {
			// new install
			MessengerPreferences.version.putPreference(preferences, versionCode);
		}
	}

	public static Preference<Boolean> startOnBoot = BooleanPreference.of("startOnBoot", true);
    public static Preference<Boolean> isOngoingNotificationEnabled = BooleanPreference.of("isOngoingNotificationEnabled", false);
	public static Preference<Integer> version = IntegerPreference.of("version", NO_VERSION);
	public static Preference<Integer> previousVersion = IntegerPreference.of("previousVersion", NO_VERSION);
	public static Preference<Integer> startCount = IntegerPreference.of("startCount", 0);


	public static final class Security {
		public static Preference<String> uuid = StringPreference.of("security.uuid", null);
		public static Preference<String> salt = StringPreference.of("security.salt", null);
	}

	public static final class Gui {

		public static Preference<MessengerTheme> theme = StringPreference.ofEnum("gui.theme", MessengerTheme.holo, MessengerTheme.class);

		public static final class Chat {

			public static final class Message {
				public static Preference<Boolean> showIcon = BooleanPreference.of("gui.chat.message.showIcon", false);
			}
		}

	}

	public static boolean isNewInstallation() {
		return MessengerPreferences.previousVersion.getPreference(getPreferences()) == NO_VERSION;
	}
}
