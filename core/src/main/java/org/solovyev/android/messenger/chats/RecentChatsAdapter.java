package org.solovyev.android.messenger.chats;

import android.content.Context;

import javax.annotation.Nonnull;

public class RecentChatsAdapter extends AbstractChatsAdapter {

	public RecentChatsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected boolean canAddChat(@Nonnull Chat chat) {
		final ChatMessage lastMessage = getChatService().getLastMessage(chat.getEntity());
		return lastMessage != null;
	}
}
