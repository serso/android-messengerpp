package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.core.R;

public enum MessageLayout {
	match_parent {
		@Override
		public int getLayoutResId(boolean userMessage) {
			return userMessage ? R.layout.mpp_list_item_message_mp_user : R.layout.mpp_list_item_message_mp_contact;
		}
	},

	wrap_content {
		public int getLayoutResId(boolean userMessage) {
			return userMessage ? R.layout.mpp_list_item_message_wc_user : R.layout.mpp_list_item_message_wc_contact;
		}
	};

	public abstract int getLayoutResId(boolean userMessage);
}
