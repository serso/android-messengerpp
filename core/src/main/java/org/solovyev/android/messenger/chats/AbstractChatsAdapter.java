package org.solovyev.android.messenger.chats;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.App.getAccountService;


abstract class AbstractChatsAdapter extends MessengerListItemAdapter<ChatListItem> /*implements ChatEventListener, UserEventListener*/ {

	public AbstractChatsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ChatListItem>(), false);
	}

	/*@Override*/
	public void onEvent(@Nonnull UserEvent event) {
		final User eventUser = event.getUser();
		switch (event.getType()) {
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
				addAll(Iterables.transform(Iterables.filter(chats, new Predicate<Chat>() {
					@Override
					public boolean apply(@Nullable Chat chat) {
						return chat != null && canAddChat(chat);
					}
				}), new Function<Chat, ChatListItem>() {
					@Override
					public ChatListItem apply(@Nullable Chat chat) {
						assert chat != null;
						return ChatListItem.newInstance(eventUser, chat);
					}
				}));
				break;
		}
	}

	protected abstract boolean canAddChat(@Nonnull Chat chat);

	protected void addAll(@Nonnull Iterable<ChatListItem> iterable) {
		super.addAll(newArrayList(iterable));
	}

	protected void removeListItem(@Nonnull User user, @Nonnull String chatId) {
		removeListItem(user, ChatImpl.newFakeChat(chatId));
	}

	protected void removeListItem(@Nonnull User user, @Nonnull Chat chat) {
		remove(ChatListItem.newEmpty(user, chat));
	}

	protected void addListItem(@Nonnull User user, @Nonnull Chat chat) {
		add(ChatListItem.newInstance(user, chat));
	}

	@Override
	protected Comparator<? super ChatListItem> getComparator() {
		return ChatListItemComparator.getInstance();
	}

	@Nonnull
	protected ChatService getChatService() {
		return App.getChatService();
	}

	/*@Override*/
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
						chatListItem.onEvent(event);
						notifyDataSetChanged();
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
