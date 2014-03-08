package org.solovyev.android.messenger.messages;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;

public enum MessageStyle {

	hangouts_white(R.drawable.msg_bubble_right_selector,
			R.drawable.msg_bubble_right_selector,
			R.drawable.msg_bubble_left_selector,
			R.drawable.msg_bubble_left_selector,
			R.color.mpp_text,
			R.color.mpp_text_secondary,
			R.dimen.mpp_hangouts_message_arrow_size,
			R.dimen.mpp_hangouts_message_shadow_size),

	hangouts_grey(R.drawable.msg_bubble_otr_right_selector,
			R.drawable.msg_bubble_otr_right_selector,
			R.drawable.msg_bubble_otr_left_selector,
			R.drawable.msg_bubble_otr_left_selector,
			R.color.mpp_text,
			R.color.mpp_text_secondary,
			R.dimen.mpp_hangouts_message_arrow_size,
			R.dimen.mpp_hangouts_message_shadow_size),

	grey(R.drawable.mpp_message_bubble_right_gray,
			R.drawable.mpp_message_bubble_right_gray_up,
			R.drawable.mpp_message_bubble_left_gray,
			R.drawable.mpp_message_bubble_left_gray_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary,
			R.dimen.mpp_message_arrow_size, 0),

	light_grey(R.drawable.mpp_message_bubble_right_gray_light,
			R.drawable.mpp_message_bubble_right_gray_light_up,
			R.drawable.mpp_message_bubble_left_gray_light,
			R.drawable.mpp_message_bubble_left_gray_light_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary,
			R.dimen.mpp_message_arrow_size, 0),

	light_blue(R.drawable.mpp_message_bubble_right_blue_light,
			R.drawable.mpp_message_bubble_right_blue_light_up,
			R.drawable.mpp_message_bubble_left_blue_light,
			R.drawable.mpp_message_bubble_left_blue_light_up,
			R.color.mpp_text,
			R.color.mpp_text_secondary,
			R.dimen.mpp_message_arrow_size, 0),

	blue(R.drawable.mpp_message_bubble_right_blue,
			R.drawable.mpp_message_bubble_right_blue_up,
			R.drawable.mpp_message_bubble_left_blue,
			R.drawable.mpp_message_bubble_left_blue_up,
			R.color.mpp_text_inverted,
			R.color.mpp_text_inverted,
			R.dimen.mpp_message_arrow_size, 0);

	private final int userDrawable;
	private final int userDrawableUp;
	private final int contactDrawable;
	private final int contactDrawableUp;
	private final int textColorResId;
	private final int secondaryTextColorResId;
	private final int arrowSizeResId;
	private final int shadowSizeResId;

	MessageStyle(int userDrawable,
				 int userDrawableUp,
				 int contactDrawable,
				 int contactDrawableUp,
				 int textColorResId, int secondaryTextColorResId, int arrowSizeResId, int shadowSizeResId) {
		this.userDrawable = userDrawable;
		this.userDrawableUp = userDrawableUp;
		this.contactDrawable = contactDrawable;
		this.contactDrawableUp = contactDrawableUp;
		this.textColorResId = textColorResId;
		this.secondaryTextColorResId = secondaryTextColorResId;
		this.arrowSizeResId = arrowSizeResId;
		this.shadowSizeResId = shadowSizeResId;
	}

	private static void applyTextColor(Resources resources, TextView textView, int colorResId) {
		final int textColor = resources.getColor(colorResId);
		textView.setTextColor(textColor);
		textView.setHintTextColor(textColor);
		textView.setLinkTextColor(textColor);
		textView.setHighlightColor(textColor);
	}

	public void prepareLayout(boolean userMessage, @Nonnull ViewAwareTag viewTag) {
		final View view = viewTag.getView();
		final Resources resources = view.getResources();

		final View messageLayout = viewTag.getViewById(R.id.mpp_li_message_linearlayout);
		final TextView messageText = viewTag.getViewById(R.id.mpp_li_message_body_textview);
		final TextView messageDateText = viewTag.getViewById(R.id.mpp_li_message_date_textview);

		messageLayout.setBackgroundResource(getDrawableResId(userMessage));

		final int arrowSize = resources.getDimensionPixelSize(arrowSizeResId);
		final int iconSize = resources.getDimensionPixelSize(R.dimen.mpp_list_item_icon_size);
		final int margin = iconSize - arrowSize;
		if (userMessage) {
			messageLayout.setPadding(0, 0, arrowSize, 0);
			((ViewGroup.MarginLayoutParams) messageLayout.getLayoutParams()).rightMargin = margin;
		} else {
			messageLayout.setPadding(arrowSize, 0, 0, 0);
			((ViewGroup.MarginLayoutParams) messageLayout.getLayoutParams()).leftMargin = margin;
		}

		if (shadowSizeResId != 0) {
			final int shadowSize = resources.getDimensionPixelSize(shadowSizeResId);
			if (shadowSize > 0) {
				view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + shadowSize, view.getPaddingRight(), view.getPaddingBottom());

				messageLayout.setMinimumHeight(iconSize + shadowSize);
				if (userMessage) {
					final ImageView messageIcon = viewTag.getViewById(R.id.mpp_li_message_icon_imageview);
					((ViewGroup.MarginLayoutParams) messageIcon.getLayoutParams()).bottomMargin = shadowSize;
				}
			}
		}

		applyTextColor(resources, messageText, textColorResId);
		applyTextColor(resources, messageDateText, secondaryTextColorResId);
	}

	private int getDrawableResId(boolean userMessage) {
		if (userMessage) {
			return userDrawable;
		} else {
			return contactDrawableUp;
		}
	}
}
