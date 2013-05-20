package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;

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
		final ChatMessage llm = lhs.getLastMessage();
		final ChatMessage rlm = rhs.getLastMessage();
		if (llm != null && rlm != null) {
			return -llm.getSendDate().compareTo(rlm.getSendDate());
		} else if (llm != null) {
			return -1;
		} else if (rlm != null) {
			return 1;
		} else {
			return 0;
		}
	}
}
