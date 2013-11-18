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
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_clicked;
import static org.solovyev.android.messenger.chats.UiChat.loadUiChat;
import static org.solovyev.android.messenger.chats.UiChat.newEmptyUiChat;
import static org.solovyev.android.messenger.messages.Messages.getMessageTime;
import static org.solovyev.android.messenger.messages.Messages.getMessageTitle;
import static org.solovyev.android.messenger.users.Users.fillContactPresenceViews;

public class ChatListItem extends AbstractMessengerListItem<UiChat> {

	@Nonnull
	private static final String TAG_PREFIX = "chat_list_item_";

	private ChatListItem(@Nonnull UiChat chat) {
		super(TAG_PREFIX, chat, R.layout.mpp_list_item_chat);
		setDisplayName(chat.getDisplayName());
	}

	@Nonnull
	public static ChatListItem newChatListItem(@Nonnull User user, @Nonnull Chat chat) {
		return new ChatListItem(loadUiChat(user, chat));
	}

	@Nonnull
	public static ChatListItem newInstance(@Nonnull UiChat data) {
		return new ChatListItem(data);
	}

	@Nonnull
	public static ChatListItem newEmpty(User user, Chat chat) {
		return new ChatListItem(newEmptyUiChat(user, chat));
	}

	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter) {
				getEventManager(context).fire(chat_clicked.newEvent(getChat()));
			}
		};
	}

	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}

	@Nonnull
	public User getUser() {
		return getData().getUser();
	}

	@Nonnull
	public Chat getChat() {
		return getData().getChat();
	}

	@Nullable
	public Message getLastMessage() {
		return this.getData().getLastMessage();
	}

	@Nonnull
	@Override
	protected String getDisplayName(@Nonnull UiChat uiChat, @Nonnull Context context) {
		return Chats.getDisplayName(uiChat.getChat(), uiChat.getLastMessage(), uiChat.getUser(), uiChat.getUnreadMessagesCount());
	}

	@Override
	protected void fillView(@Nonnull UiChat uiChat, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final Chat chat = uiChat.getChat();
		final User user = uiChat.getUser();

		final ImageView chatIcon = viewTag.getViewById(R.id.mpp_li_chat_icon_imageview);
		getChatService().setChatIcon(chat, chatIcon);

		final Message lastMessage = getLastMessage();

		final TextView chatTitle = viewTag.getViewById(R.id.mpp_li_chat_title_textview);
		chatTitle.setText(getDisplayName());
		if (uiChat.getUnreadMessagesCount() > 0) {
			chatTitle.setTypeface(null, Typeface.BOLD);
		} else {
			chatTitle.setTypeface(null, Typeface.NORMAL);
		}

		final TextView lastMessageTextTime = viewTag.getViewById(R.id.mpp_li_last_message_text_time_textview);
		final TextView lastMessageText = viewTag.getViewById(R.id.mpp_li_last_message_text_textview);
		if (lastMessage != null) {
			lastMessageText.setText(getMessageTitle(chat, lastMessage, user));
			lastMessageTextTime.setText(getMessageTime(lastMessage));
		} else {
			lastMessageText.setText("");
			lastMessageTextTime.setText("");
		}

		if (chat.isPrivate()) {
			fillContactPresence(uiChat, viewTag, context);
		} else {
			fillContactPresences(uiChat, viewTag);
		}
	}

	private void fillContactPresence(@Nonnull UiChat uiChat, @Nonnull ViewAwareTag viewTag, @Nonnull Context context) {
		final Entity secondUser = uiChat.getChat().getSecondUser();
		fillContactPresenceViews(context, viewTag, getUserService().getUserById(secondUser), uiChat.getAccount());
	}

	private void fillContactPresences(UiChat uiChat, ViewAwareTag viewTag) {
		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		viewTag.getViewById(R.id.mpp_li_contact_call_view).setVisibility(GONE);
		viewTag.getViewById(R.id.mpp_li_contact_divider_view).setVisibility(GONE);
		if (uiChat.isOnline()) {
			contactOnline.setVisibility(VISIBLE);
		} else {
			contactOnline.setVisibility(GONE);
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	public boolean onEvent(@Nonnull UserEvent event) {
		boolean changed = false;

		switch (event.getType()) {
			case contacts_presence_changed:
				changed = getData().updateOnlineStatus();
				if (changed) {
					onDataChanged();
				}
				break;
		}

		return changed;
	}

	public boolean onEvent(@Nonnull ChatEvent event) {
		boolean changed = false;

		final Chat chat = getChat();
		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case changed:
				if (eventChat.equals(chat)) {
					getData().setChat(eventChat);
					onDataChanged();
					changed = true;
				}
				break;

			case last_message_changed:
				if (eventChat.equals(chat)) {
					getData().setLastMessage(event.getDataAsMessage());
					onDataChanged();
					changed = true;
				}
				break;
			case unread_message_count_changed:
				if (eventChat.equals(chat)) {
					getData().setUnreadMessagesCount(event.getDataAsInteger());
					onDataChanged();
					changed = true;
				}
				break;
		}

		return changed;
	}
}
