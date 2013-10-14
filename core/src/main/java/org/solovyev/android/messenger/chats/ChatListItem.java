package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_clicked;
import static org.solovyev.android.messenger.chats.UiChat.loadUiChat;
import static org.solovyev.android.messenger.chats.UiChat.newEmptyUiChat;
import static org.solovyev.android.messenger.messages.Messages.getMessageTime;
import static org.solovyev.android.messenger.messages.Messages.getMessageTitle;
import static org.solovyev.android.messenger.users.Users.fillContactPresenceViews;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:24 PM
 */
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
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
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

		final TextView lastMessageTextTime = viewTag.getViewById(R.id.mpp_li_last_message_text_time_textview);
		final TextView lastMessageText = viewTag.getViewById(R.id.mpp_li_last_message_text_textview);
		if (lastMessage != null) {
			lastMessageText.setText(getMessageTitle(chat, lastMessage, user));
			lastMessageTextTime.setText(getMessageTime(lastMessage));
		} else {
			lastMessageText.setText("");
			lastMessageTextTime.setText("");
		}

		if(chat.isPrivate()) {
			fillContactPresence(uiChat, viewTag, context);
		} else {
			fillContactPresences(uiChat, viewTag);
		}
	}

	private void fillContactPresence(@Nonnull UiChat uiChat, @Nonnull ViewAwareTag viewTag, @Nonnull Context context) {
		final Entity secondUser = uiChat.getChat().getSecondUser();
		fillContactPresenceViews(context, viewTag, getUserService().getUserById(secondUser, false), uiChat.getAccount());
	}

	private void fillContactPresences(UiChat uiChat, ViewAwareTag viewTag) {
		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		if (uiChat.isOnline()) {
			contactOnline.setVisibility(VISIBLE);
		} else {
			contactOnline.setVisibility(INVISIBLE);
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
					getData().setLastMessage(event.getDataAsChatMessage());
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
