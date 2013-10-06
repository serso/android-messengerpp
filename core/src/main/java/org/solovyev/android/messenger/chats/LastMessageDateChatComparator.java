package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.messages.ChatMessage;

import java.util.Comparator;

import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;

class LastMessageDateChatComparator implements Comparator<UiChat> {

	@Override
	public int compare(UiChat lhs, UiChat rhs) {
		final ChatMessage lm = lhs.getLastMessage();
		final ChatMessage rm = rhs.getLastMessage();
		return compareSendDatesLatestFirst(lm, rm);
	}

}
