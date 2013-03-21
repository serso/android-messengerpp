package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.solovyev.android.messenger.MessengerPreferences;

import javax.annotation.Nonnull;

final class MessageListItemStyle {

    @Nonnull
    private final MessengerPreferences.Gui.Chat.Message.Style userMessageStyle;

    @Nonnull
    private final MessengerPreferences.Gui.Chat.Message.Style contactMessageStyle;

    private MessageListItemStyle(@Nonnull MessengerPreferences.Gui.Chat.Message.Style userMessageStyle,
                                 @Nonnull MessengerPreferences.Gui.Chat.Message.Style contactMessageStyle) {
        this.userMessageStyle = userMessageStyle;
        this.contactMessageStyle = contactMessageStyle;
    }

    @Nonnull
    static MessageListItemStyle newFromDefaultPreferences(@Nonnull Context context) {
        return newFromPreferences(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @Nonnull
    static MessageListItemStyle newFromPreferences(@Nonnull SharedPreferences preferences) {
        final MessengerPreferences.Gui.Chat.Message.Style userMessageStyle = MessengerPreferences.Gui.Chat.Message.userMessageStyle.getPreference(preferences);
        final MessengerPreferences.Gui.Chat.Message.Style contactMessageStyle = MessengerPreferences.Gui.Chat.Message.contactMessageStyle.getPreference(preferences);
        return new MessageListItemStyle(userMessageStyle, contactMessageStyle);
    }

    int getButtonDrawableResId(boolean userMessage) {
        return getMessageStyle(userMessage).getButtonDrawableResId();
    }

    private MessengerPreferences.Gui.Chat.Message.Style getMessageStyle(boolean userMessage) {
        return userMessage ? userMessageStyle : contactMessageStyle;
    }

    int getTextColorResId(boolean userMessage) {
        return getMessageStyle(userMessage).getTextColorResId();
    }

    int getMessageBackground(boolean userMessage) {
        return getMessageStyle(userMessage).getMessageBackground(userMessage);
    }

    int getButtonTextColorResId(boolean userMessage) {
        return getMessageStyle(userMessage).getButtonTextColorResId();
    }
}
