package org.solovyev.android.messenger.chats;

import java.util.Comparator;

class LastMessageDateChatComparator implements Comparator<UiChat> {

	@Override
	public int compare(UiChat lhs, UiChat rhs) {
		final ChatMessage rm = rhs.getLastMessage();
		final ChatMessage lm = lhs.getLastMessage();
		if(lm == null && rm == null) {
			return 0;
		} else if (lm == null) {
			return -1;
		} else if (rm == null) {
			return 1;
		} else {
			return rm.getSendDate().compareTo(lm.getSendDate());
		}
	}
}
