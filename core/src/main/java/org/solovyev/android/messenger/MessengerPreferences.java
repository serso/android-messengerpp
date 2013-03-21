package org.solovyev.android.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.Preference;
import org.solovyev.android.prefs.StringPreference;

import javax.annotation.Nonnull;

public final class MessengerPreferences {

    public static void setDefaultValues(@Nonnull Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Gui.Chat.Message.showIcon.tryPutDefault(preferences);
        Gui.Chat.Message.userMessageStyle.tryPutDefault(preferences);
        Gui.Chat.Message.contactMessageStyle.tryPutDefault(preferences);

    }

    public static final class Gui {
        public static final class Chat {

            public static final class Message {
                public static Preference<Boolean> showIcon = BooleanPreference.of("gui.chat.message.showIcon", false);
                public static Preference<Gui.Chat.Message.Style> userMessageStyle = StringPreference.ofEnum("gui.chat.message.userMessageStyle", Gui.Chat.Message.Style.metro_gray_light, Gui.Chat.Message.Style.class);
                public static Preference<Gui.Chat.Message.Style> contactMessageStyle = StringPreference.ofEnum("gui.chat.message.contactMessageStyle", Gui.Chat.Message.Style.metro_gray, Gui.Chat.Message.Style.class);

                public static enum Style {
                    metro_blue(R.drawable.mpp_message_bubble_left_blue, R.drawable.mpp_message_bubble_right_blue),
                    metro_gray(R.drawable.mpp_message_bubble_left_gray, R.drawable.mpp_message_bubble_right_gray),
                    metro_gray_light(R.drawable.mpp_message_bubble_left_gray_light, R.drawable.mpp_message_bubble_right_gray_light);

                    private final int leftMessageBackground;

                    private final int rightMessageBackground;

                    Style(int leftMessageBackground, int rightMessageBackground) {
                        this.leftMessageBackground = leftMessageBackground;
                        this.rightMessageBackground = rightMessageBackground;
                    }

                    public int getLeftMessageBackground() {
                        return leftMessageBackground;
                    }

                    public int getRightMessageBackground() {
                        return rightMessageBackground;
                    }
                }
            }
        }
    }
}
