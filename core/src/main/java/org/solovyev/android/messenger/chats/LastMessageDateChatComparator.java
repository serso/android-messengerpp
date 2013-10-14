package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.messages.Message;

import java.util.Comparator;

import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;

class LastMessageDateChatComparator implements Comparator<UiChat> {

	@Override
	public int compare(UiChat lhs, UiChat rhs) {
		final Message lm = lhs.getLastMessage();
		final Message rm = rhs.getLastMessage();
		return compareSendDatesLatestFirst(lm, rm);
	}

}
