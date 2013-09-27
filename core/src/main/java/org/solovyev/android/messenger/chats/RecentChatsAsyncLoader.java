package org.solovyev.android.messenger.chats;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getChatService;
import static org.solovyev.android.messenger.chats.Chats.getLastChatsByDate;

public class RecentChatsAsyncLoader extends AbstractAsyncLoader<UiChat, ChatListItem> {

	public RecentChatsAsyncLoader(@Nonnull Context context,
								  @Nonnull ListItemAdapter<ChatListItem> adapter,
								  @Nullable Runnable onPostExecute) {
		super(context, adapter, onPostExecute);
	}

	@Nonnull
	@Override
	protected List<UiChat> getElements(@Nonnull Context context) {
		final List<UiChat> result = new ArrayList<UiChat>(Chats.MAX_RECENT_CHATS);

		final ChatService chatService = getChatService();
		final AccountService accountService = getAccountService();

		for (User user : accountService.getEnabledAccountUsers()) {
			result.addAll(chatService.getLastUserChats(user, Chats.MAX_RECENT_CHATS));
		}

		return getLastChatsByDate(result, Chats.MAX_RECENT_CHATS);
	}

	@Nonnull
	@Override
	protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
		return ChatListItem.newInstance(uiChat);
	}
}
