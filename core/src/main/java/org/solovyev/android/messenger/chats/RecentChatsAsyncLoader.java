package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getChatService;

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
		return getChatService().getLastChats(maxCount);
	}

	@Nonnull
	@Override
	protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
		return ChatListItem.newInstance(uiChat);
	}
}
