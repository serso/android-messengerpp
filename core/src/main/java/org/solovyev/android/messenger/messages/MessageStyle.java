package org.solovyev.android.messenger.messages;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;

public enum MessageStyle {

	grey(R.drawable.mpp_message_bubble_right_gray,
			R.drawable.mpp_message_bubble_right_gray_up,
			R.drawable.mpp_message_bubble_left_gray,
			R.drawable.mpp_message_bubble_left_gray_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary),

	light_grey(R.drawable.mpp_message_bubble_right_gray_light,
			R.drawable.mpp_message_bubble_right_gray_light_up,
			R.drawable.mpp_message_bubble_left_gray_light,
			R.drawable.mpp_message_bubble_left_gray_light_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary),

	light_blue(R.drawable.mpp_message_bubble_right_blue_light,
			R.drawable.mpp_message_bubble_right_blue_light_up,
			R.drawable.mpp_message_bubble_left_blue_light,
			R.drawable.mpp_message_bubble_left_blue_light_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary),

	blue(R.drawable.mpp_message_bubble_right_blue,
			R.drawable.mpp_message_bubble_right_blue_up,
			R.drawable.mpp_message_bubble_left_blue,
			R.drawable.mpp_message_bubble_left_blue_up,
			R.color.mpp_text_inverted,
			R.color.mpp_text_inverted);

	private final int userDrawable;
	private final int userDrawableUp;
	private final int contactDrawable;
	private final int contactDrawableUp;
	private final int textColorResId;
	private final int secondaryTextColorResId;

	MessageStyle(int userDrawable,
				 int userDrawableUp,
				 int contactDrawable,
				 int contactDrawableUp,
				 int textColorResId, int secondaryTextColorResId) {
		this.userDrawable = userDrawable;
		this.userDrawableUp = userDrawableUp;
		this.contactDrawable = contactDrawable;
		this.contactDrawableUp = contactDrawableUp;
		this.textColorResId = textColorResId;
		this.secondaryTextColorResId = secondaryTextColorResId;
	}

	private static void applyTextColor(Resources resources, TextView textView, int colorResId) {
		final int textColor = resources.getColor(colorResId);
		textView.setTextColor(textColor);
		textView.setHintTextColor(textColor);
		textView.setLinkTextColor(textColor);
		textView.setHighlightColor(textColor);
	}

	public void prepareLayout(boolean userMessage, boolean up, @Nonnull ViewAwareTag viewTag) {
		final Resources resources = viewTag.getView().getResources();

		final View messageLayout = viewTag.getViewById(R.id.mpp_li_message_linearlayout);
		final TextView messageText = viewTag.getViewById(R.id.mpp_li_message_body_textview);
		final TextView messageDateText = viewTag.getViewById(R.id.mpp_li_message_date_textview);

		messageLayout.setBackgroundResource(getDrawableResId(userMessage, up));

		applyTextColor(resources, messageText, textColorResId);
		applyTextColor(resources, messageDateText, secondaryTextColorResId);
	}

	private int getDrawableResId(boolean userMessage, boolean up) {
		if (userMessage) {
			return up ? userDrawableUp : userDrawable;
		} else {
			return up ? contactDrawableUp : contactDrawable;
		}
	}
}
