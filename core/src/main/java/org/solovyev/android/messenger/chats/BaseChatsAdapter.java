/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.chats;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseListItemAdapter;
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

abstract class BaseChatsAdapter extends BaseListItemAdapter<ChatListItem> {

	@Nonnull
	private String query = "";

	public BaseChatsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ChatListItem>(), false, true);
	}

	@Override
	public void onEvent(@Nonnull UserEvent event) {
		final User user = event.getUser();
		switch (event.getType()) {
			case contacts_presence_changed:
				final Iterable<ChatListItem> chatListItems = findChatListItemsForUser(user);

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
				removeListItem(user, chatId);
				break;
			case chat_added:
				final ChatListItem chatListItem = newChatListItem(user, event.getDataAsChat());
				if (canAddChat(chatListItem)) {
					add(chatListItem);
				}
				break;
			case chats_added:
				final List<Chat> chats = event.getDataAsChats();
				addAll(toChatListItems(user, chats));
				break;
		}
	}

	@Nonnull
	public String getQuery() {
		return query;
	}

	public void setQuery(@Nullable CharSequence query) {
		this.query = query == null ? "" : query.toString();
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
		final Iterable<ChatListItem> chatListItems = transform(chats, new Function<Chat, ChatListItem>() {
			@Override
			public ChatListItem apply(Chat chat) {
				return newChatListItem(eventUser, chat);
			}
		});

		return Iterables.filter(chatListItems, new Predicate<ChatListItem>() {
			@Override
			public boolean apply(ChatListItem chat) {
				return canAddChat(chat);
			}
		});
	}

	protected abstract boolean canAddChat(@Nonnull ChatListItem chat);

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
		final ChatListItem chatListItem = newChatListItem(user, chat);
		if (canAddChat(chatListItem)) {
			add(chatListItem);
		}
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
				final User user = getAccountService().getAccountById(eventChat.getEntity().getAccountId()).getUser();
				final ChatListItem chatListItem = findInAllElements(user, eventChat);
				if (chatListItem != null) {
					if (chatListItem.onEvent(event)) {
						onDataSetChanged();
					}
				}
				break;
		}
	}


	@Nullable
	protected ChatListItem findInAllElements(@Nonnull User user, @Nonnull Chat chat) {
		return Iterables.find(getAllElements(), Predicates.<ChatListItem>equalTo(ChatListItem.newEmpty(user, chat)), null);
	}
}
