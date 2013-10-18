package org.solovyev.android.messenger.chats;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.chats.ChatListItem.newChatListItem;


abstract class AbstractChatsAdapter extends BaseListItemAdapter<ChatListItem> {

	public AbstractChatsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ChatListItem>(), false, true);
	}

	/*@Override*/
	public void onEvent(@Nonnull UserEvent event) {
		final User eventUser = event.getUser();
		switch (event.getType()) {
			case contacts_presence_changed:
				final Iterable<ChatListItem> chatListItems = findChatListItemsForUser(eventUser);

				boolean changed = false;
				for (ChatListItem chatListItem : chatListItems) {
					changed |= chatListItem.onEvent(event);
				}

				if (changed) {
					onDataSetChanged();
				}
				break;
			case chat_removed:
				final String chatId = event.getDataAsChatId();
				removeListItem(eventUser, chatId);
				break;
			case chat_added:
				final Chat chat = event.getDataAsChat();
				if (canAddChat(chat)) {
					addListItem(eventUser, chat);
				}
				break;
			case chat_added_batch:
				final List<Chat> chats = event.getDataAsChats();
				addAll(toChatListItems(eventUser, chats));
				break;
		}
	}

	@Nonnull
	private Iterable<ChatListItem> findChatListItemsForUser(@Nonnull final User user) {
		return Iterables.filter(getAllElements(), new Predicate<ChatListItem>() {
			@Override
			public boolean apply(ChatListItem item) {
				return item.getUser().equals(user);
			}
		});
	}

	@Nonnull
	private Iterable<ChatListItem> toChatListItems(@Nonnull final User eventUser, @Nonnull List<Chat> chats) {
		return transform(Iterables.filter(chats, new Predicate<Chat>() {
			@Override
			public boolean apply(Chat chat) {
				return canAddChat(chat);
			}
		}), new Function<Chat, ChatListItem>() {
			@Override
			public ChatListItem apply(Chat chat) {
				return newChatListItem(eventUser, chat);
			}
		});
	}

	protected abstract boolean canAddChat(@Nonnull Chat chat);

	protected void addAll(@Nonnull Iterable<ChatListItem> iterable) {
		super.addAll(newArrayList(iterable));
	}

	protected void removeListItem(@Nonnull User user, @Nonnull String chatId) {
		removeListItem(user, Chats.newEmptyChat(chatId));
	}

	protected void removeListItem(@Nonnull User user, @Nonnull Chat chat) {
		remove(ChatListItem.newEmpty(user, chat));
	}

	protected void addListItem(@Nonnull User user, @Nonnull Chat chat) {
		add(newChatListItem(user, chat));
	}

	@Override
	protected Comparator<? super ChatListItem> getComparator() {
		return ChatListItemComparator.getInstance();
	}

	@Nonnull
	protected ChatService getChatService() {
		return App.getChatService();
	}

	public void onEvent(@Nonnull ChatEvent event) {
		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case changed:
			case last_message_changed:
			case unread_message_count_changed:
				try {
					final User user = getAccountService().getAccountById(eventChat.getEntity().getAccountId()).getUser();
					final ChatListItem chatListItem = findInAllElements(user, eventChat);
					if (chatListItem != null) {
						if (chatListItem.onEvent(event)) {
							onDataSetChanged();
						}
					}
				} catch (UnsupportedAccountException e) {
					App.getExceptionHandler().handleException(e);
				}
				break;
		}
	}



	@Nullable
	protected ChatListItem findInAllElements(@Nonnull User user, @Nonnull Chat chat) {
		return Iterables.find(getAllElements(), Predicates.<ChatListItem>equalTo(ChatListItem.newEmpty(user, chat)), null);
	}
}
