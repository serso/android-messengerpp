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

    public static final class Security {
        public static Preference<String> uuid = StringPreference.of("security.uuid", null);
        public static Preference<String> salt = StringPreference.of("security.salt", null);
    }

    public static final class Gui {
        public static final class Chat {

            public static final class Message {
                public static Preference<Boolean> showIcon = BooleanPreference.of("gui.chat.message.showIcon", false);
                public static Preference<Gui.Chat.Message.Style> userMessageStyle = StringPreference.ofEnum("gui.chat.message.userMessageStyle", Gui.Chat.Message.Style.metro_gray_light, Gui.Chat.Message.Style.class);
                public static Preference<Gui.Chat.Message.Style> contactMessageStyle = StringPreference.ofEnum("gui.chat.message.contactMessageStyle", Style.metro_blue, Gui.Chat.Message.Style.class);

                public static enum Style {
                    metro_blue(R.drawable.mpp_message_bubble_left_blue, R.drawable.mpp_message_bubble_right_blue, R.color.mpp_text_inverted, R.drawable.mpp_metro_button_white, R.color.mpp_text),
                    metro_gray(R.drawable.mpp_message_bubble_left_gray, R.drawable.mpp_message_bubble_right_gray, R.color.mpp_text, R.drawable.mpp_metro_button_white, R.color.mpp_text),
                    metro_gray_light(R.drawable.mpp_message_bubble_left_gray_light, R.drawable.mpp_message_bubble_right_gray_light, R.color.mpp_text, R.drawable.mpp_metro_button_gray, R.color.mpp_text);

                    private final int leftMessageBackground;

                    private final int rightMessageBackground;

                    private final int textColorResId;

                    private final int buttonDrawableResId;

                    private final int buttonTextColorResId;

                    Style(int leftMessageBackground, int rightMessageBackground, int textColorResId, int buttonDrawableResId, int buttonTextColorResId) {
                        this.leftMessageBackground = leftMessageBackground;
                        this.rightMessageBackground = rightMessageBackground;
                        this.textColorResId = textColorResId;
                        this.buttonDrawableResId = buttonDrawableResId;
                        this.buttonTextColorResId = buttonTextColorResId;
                    }

                    public int getButtonDrawableResId() {
                        return buttonDrawableResId;
                    }

                    public int getTextColorResId() {
                        return textColorResId;
                    }

                    public int getMessageBackground(boolean userMessage) {
                        return userMessage ? leftMessageBackground : rightMessageBackground;
                    }

                    public int getButtonTextColorResId() {
                        return buttonTextColorResId;
                    }
                }
            }
        }
    }
}
