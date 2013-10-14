package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.messages.Message;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:51 PM
 */
final class ChatListItemComparator implements java.util.Comparator<ChatListItem> {

	@Nonnull
	private static final ChatListItemComparator instance = new ChatListItemComparator();

	ChatListItemComparator() {
	}

	@Nonnull
	static ChatListItemComparator getInstance() {
		return instance;
	}

	@Override
	public int compare(@Nonnull ChatListItem lhs, @Nonnull ChatListItem rhs) {
		final Message lm = lhs.getLastMessage();
		final Message rm = rhs.getLastMessage();
		return compareSendDatesLatestFirst(lm, rm);
	}
}
