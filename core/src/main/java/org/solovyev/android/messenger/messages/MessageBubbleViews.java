package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.Views;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerPreferences;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.users.User;

final class MessageBubbleViews {

	private MessageBubbleViews() {
	}

	static void fillMessageBubbleViews(@Nonnull Context context,
									   @Nonnull View messageLayoutParent,
									   @Nonnull View messageLayout,
									   @Nonnull TextView messageText,
									   @Nullable TextView messageDate,
									   boolean userMessage,
									   boolean processButtons) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final MessageListItemStyle style = MessageListItemStyle.newFromPreferences(preferences);
		fillMessageBubbleViews(context, messageLayoutParent, messageLayout, messageText, messageDate, userMessage, processButtons, style);
	}

	static void fillMessageBubbleViews(@Nonnull Context context,
									   @Nonnull View messageLayoutParent,
									   @Nonnull View messageLayout,
									   @Nonnull TextView messageText,
									   @Nullable TextView messageDate,
									   boolean userMessage,
									   boolean processButtons,
									   @Nonnull MessageListItemStyle style) {
		applyMessageBubblePaddings(context, messageLayoutParent, messageLayout, userMessage);
		applyMessageBubbleStyles(context, messageLayout, messageText, messageDate, userMessage, processButtons, style);
	}

	private static void applyMessageBubbleStyles(@Nonnull Context context,
												 @Nonnull View messageLayout,
												 @Nonnull TextView messageText,
												 @Nullable TextView messageDate,
												 final boolean userMessage,
												 boolean processButtons,
												 @Nonnull final MessageListItemStyle style) {
		final Resources resources = context.getResources();

		messageLayout.setBackgroundDrawable(resources.getDrawable(style.getMessageBackground(userMessage)));
		final int textColor = resources.getColor(style.getTextColorResId(userMessage));
		messageText.setTextColor(textColor);
		messageText.setHintTextColor(textColor);
		messageText.setLinkTextColor(textColor);
		messageText.setHighlightColor(textColor);
		if (messageDate != null) {
			messageDate.setTextColor(textColor);
			messageDate.setHintTextColor(textColor);
			messageDate.setLinkTextColor(textColor);
			messageDate.setHighlightColor(textColor);
		}

		if (processButtons) {
			Views.processViewsOfType(messageLayout, Button.class, new Views.ViewProcessor<Button>() {
				@Override
				public void process(@Nonnull Button button) {
					button.setBackgroundDrawable(resources.getDrawable(style.getButtonDrawableResId(userMessage)));
					button.setTextColor(resources.getColor(style.getButtonTextColorResId(userMessage)));
				}
			});
		}
	}

	private static void applyMessageBubblePaddings(@Nonnull Context context,
												   @Nonnull View messageLayoutParent,
												   @Nonnull View messageLayout,
												   boolean userMessage) {
		final Resources resources = context.getResources();
		final DisplayMetrics dm = resources.getDisplayMetrics();

		final int outerPaddingDps = Views.toPixels(dm, 2);
		final int outerSidePaddingDps = Views.toPixels(dm, 14);
		final int innerPaddingDps = Views.toPixels(dm, 5);
		final int innerSidePaddingDps = Views.toPixels(dm, 25);
		if (userMessage) {
			messageLayoutParent.setPadding(outerPaddingDps, outerPaddingDps, outerSidePaddingDps, outerPaddingDps);
			messageLayout.setPadding(innerSidePaddingDps, innerPaddingDps, innerPaddingDps, innerPaddingDps);
		} else {
			messageLayoutParent.setPadding(outerSidePaddingDps, outerPaddingDps, outerPaddingDps, outerPaddingDps);
			messageLayout.setPadding(innerPaddingDps, innerPaddingDps, innerSidePaddingDps, innerPaddingDps);
		}
	}

	static void setMessageBubbleMessageIcon(@Nonnull Context context, @Nonnull ChatMessage message, @Nonnull ImageView messageIcon) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (MessengerPreferences.Gui.Chat.Message.showIcon.getPreference(preferences)) {
			messageIcon.setVisibility(View.VISIBLE);
			App.getChatMessageService().setMessageIcon(message, messageIcon);
		} else {
			messageIcon.setVisibility(View.GONE);
		}
	}

	static void setMessageBubbleUserIcon(@Nonnull Context context, @Nonnull User user, @Nonnull ImageView userIconImageView) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (MessengerPreferences.Gui.Chat.Message.showIcon.getPreference(preferences)) {
			userIconImageView.setVisibility(View.VISIBLE);
			App.getUserService().setUserIcon(user, userIconImageView);
		} else {
			userIconImageView.setVisibility(View.GONE);
		}
	}
}
