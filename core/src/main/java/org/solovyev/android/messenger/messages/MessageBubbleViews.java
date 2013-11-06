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

package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.Views;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerPreferences;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.User;

final class MessageBubbleViews {

	private MessageBubbleViews() {
	}

	static void fillMessageBubbleViews(@Nonnull Context context,
									   @Nonnull View messageLayoutParent,
									   @Nonnull View messageLayout,
									   @Nonnull TextView messageText,
									   @Nullable TextView messageDate,
									   boolean userMessage) {
		applyMessageBubblePaddings(context, messageLayoutParent, messageLayout, userMessage);
		applyMessageBubbleStyles(context, messageLayout, messageText, messageDate, userMessage);
	}

	private static void applyMessageBubbleStyles(@Nonnull Context context,
												 @Nonnull View messageLayout,
												 @Nonnull TextView messageText,
												 @Nullable TextView messageDate,
												 final boolean userMessage) {
		final Resources resources = context.getResources();

		messageLayout.setBackgroundDrawable(resources.getDrawable(userMessage ? R.drawable.mpp_message_bubble_left_gray_light : R.drawable.mpp_message_bubble_right_blue));
		final int textColor = resources.getColor(userMessage ?  R.color.mpp_text : R.color.mpp_text_inverted);
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

	static void setMessageBubbleMessageIcon(@Nonnull Context context, @Nonnull Message message, @Nonnull ImageView messageIcon) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (MessengerPreferences.Gui.Chat.Message.showIcon.getPreference(preferences)) {
			messageIcon.setVisibility(View.VISIBLE);
			App.getMessageService().setMessageIcon(message, messageIcon);
		} else {
			messageIcon.setVisibility(View.GONE);
		}
	}
}
