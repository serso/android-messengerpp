package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getChatService;
import static org.solovyev.android.messenger.chats.Chats.getLastChatsByDate;

public class RecentChatsAsyncLoader extends AbstractAsyncLoader<UiChat, ChatListItem> {

	private final int maxCount;

	public RecentChatsAsyncLoader(@Nonnull Context context,
								  @Nonnull ListItemAdapter<ChatListItem> adapter,
								  @Nullable Runnable onPostExecute,
								  int maxCount) {
		super(context, adapter, onPostExecute);
		this.maxCount = maxCount;
	}

	@Nonnull
	@Override
	protected List<UiChat> getElements(@Nonnull Context context) {
		final List<UiChat> result = new ArrayList<UiChat>(maxCount);

		final ChatService chatService = getChatService();
		final AccountService accountService = getAccountService();

		for (User user : accountService.getEnabledAccountUsers()) {
			result.addAll(chatService.getLastChats(user, maxCount));
		}

		return getLastChatsByDate(result, maxCount);
	}

	@Nonnull
	@Override
	protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
		return ChatListItem.newInstance(uiChat);
	}
}
