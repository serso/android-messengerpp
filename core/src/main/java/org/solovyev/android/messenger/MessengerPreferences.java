package org.solovyev.android.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.core.R;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;

public final class MessengerPreferences {

	public static void setDefaultValues(@Nonnull Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		Gui.Chat.Message.showIcon.tryPutDefault(preferences);

	}

	public static final class Security {
		public static Preference<String> uuid = StringPreference.of("security.uuid", null);
		public static Preference<String> salt = StringPreference.of("security.salt", null);
	}

	public static final class Gui {
		public static final class Chat {

			public static final class Message {
				public static Preference<Boolean> showIcon = BooleanPreference.of("gui.chat.message.showIcon", false);
			}
		}
	}
}
