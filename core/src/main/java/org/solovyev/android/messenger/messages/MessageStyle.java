package org.solovyev.android.messenger.messages;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;

public enum MessageStyle {

	grey(R.drawable.mpp_message_bubble_right_gray, R.drawable.mpp_message_bubble_left_gray, R.color.mpp_text),
	light_grey(R.drawable.mpp_message_bubble_right_gray_light, R.drawable.mpp_message_bubble_left_gray_light, R.color.mpp_text),
	light_blue(R.drawable.mpp_message_bubble_right_blue_light, R.drawable.mpp_message_bubble_left_blue_light, R.color.mpp_text),
	blue(R.drawable.mpp_message_bubble_right_blue, R.drawable.mpp_message_bubble_left_blue, R.color.mpp_text_inverted);

	private final int userDrawable;
	private final int contactDrawable;
	private final int textColorResId;

	MessageStyle(int userDrawable, int contactDrawable, int textColorResId) {
		this.userDrawable = userDrawable;
		this.contactDrawable = contactDrawable;
		this.textColorResId = textColorResId;
	}

	private static void applyTextColor(Resources resources, TextView textView, int colorResId) {
		final int textColor = resources.getColor(colorResId);
		textView.setTextColor(textColor);
		textView.setHintTextColor(textColor);
		textView.setLinkTextColor(textColor);
		textView.setHighlightColor(textColor);
	}

	public void prepareLayout(boolean userMessage, @Nonnull ViewAwareTag viewTag) {
		final Resources resources = viewTag.getView().getResources();

		final View messageLayout = viewTag.getViewById(R.id.mpp_li_message_linearlayout);
		final TextView messageText = viewTag.getViewById(R.id.mpp_li_message_body_textview);
		final TextView messageDateText = viewTag.getViewById(R.id.mpp_li_message_date_textview);

		messageLayout.setBackgroundResource(userMessage ? userDrawable : contactDrawable);

		applyTextColor(resources, messageText, textColorResId);
		applyTextColor(resources, messageDateText, textColorResId);
	}
}
