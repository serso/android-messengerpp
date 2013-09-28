package org.solovyev.android.messenger.chats;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:23 PM
 */
final class ChatsAsyncLoader extends AbstractAsyncLoader<UiChat, ChatListItem> {

	ChatsAsyncLoader(@Nonnull Context context, @Nonnull ListItemAdapter<ChatListItem> adapter, @Nullable Runnable onPostExecute) {
		super(context, adapter, onPostExecute);
	}

	@Nonnull
	@Override
	protected List<UiChat> getElements(@Nonnull Context context) {
		final List<UiChat> result = new ArrayList<UiChat>();

		final UserService userService = App.getUserService();
		final ChatService chatService = App.getChatService();
		final AccountService accountService = App.getAccountService();

		for (User user : accountService.getEnabledAccountUsers()) {
			final List<Chat> chats = userService.getUserChats(user.getEntity());
			for (Chat chat : chats) {
				final ChatMessage lastMessage = chatService.getLastMessage(chat.getEntity());
				final int unreadMessagesCount = chatService.getUnreadMessagesCount(chat.getEntity());
				final String displayName = Chats.getDisplayName(chat, lastMessage, user, unreadMessagesCount);
				result.add(UiChat.newUiChat(user, chat, lastMessage, unreadMessagesCount, displayName));
			}
		}

		return result;
	}

	@Nonnull
	@Override
	protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
		return ChatListItem.newInstance(uiChat);
	}

}
