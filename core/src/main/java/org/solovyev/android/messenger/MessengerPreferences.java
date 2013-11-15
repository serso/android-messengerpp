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

import javax.annotation.Nonnull;

import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;

public final class MessengerPreferences {

	public static void setDefaultValues(@Nonnull Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		Gui.theme.tryPutDefault(preferences);
		Gui.Chat.Message.showIcon.tryPutDefault(preferences);

	}

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
}
