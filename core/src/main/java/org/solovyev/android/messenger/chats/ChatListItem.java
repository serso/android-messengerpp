package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_clicked;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:24 PM
 */
public class ChatListItem extends AbstractMessengerListItem<UiChat> /*implements ChatEventListener*/ {

	@Nonnull
	private static final String TAG_PREFIX = "chat_list_item_";

	private ChatListItem(@Nonnull UiChat chat) {
		super(TAG_PREFIX, chat, R.layout.mpp_list_item_chat);
		setDisplayName(chat.getDisplayName());
	}

	@Nullable
	private static ChatMessage getLastChatMessage(Chat chat) {
		return getChatService().getLastMessage(chat.getEntity());
	}

	private static int getUnreadMessagesCount(@Nonnull Chat chat) {
		return getChatService().getUnreadMessagesCount(chat.getEntity());
	}

	@Nonnull
	public static ChatListItem newInstance(@Nonnull User user, @Nonnull Chat chat) {
		return new ChatListItem(UiChat.newInstance(user, chat, getLastChatMessage(chat), getUnreadMessagesCount(chat), ""));
	}

	@Nonnull
	public static ChatListItem newInstance(@Nonnull UiChat data) {
		return new ChatListItem(data);
	}

	@Nonnull
	public static ChatListItem newEmpty(User user, Chat chat) {
		return new ChatListItem(UiChat.newInstance(user, chat, null, 0, ""));
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
	public ChatMessage getLastMessage() {
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

		final ChatMessage lastMessage = getLastMessage();

		final TextView chatTitle = viewTag.getViewById(R.id.mpp_li_chat_title_textview);
		chatTitle.setText(getDisplayName());

		final TextView lastMessageTextTime = viewTag.getViewById(R.id.mpp_li_last_message_text_time_textview);
		final TextView lastMessageText = viewTag.getViewById(R.id.mpp_li_last_message_text_textview);
		if (lastMessage != null) {
			lastMessageText.setText(Messages.getMessageTitle(chat, lastMessage, user));
			lastMessageTextTime.setText(Messages.getMessageTime(lastMessage));
		} else {
			lastMessageText.setText("");
			lastMessageTextTime.setText("");
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return App.getChatService();
	}

	/*@Override*/
	public void onEvent(@Nonnull ChatEvent event) {
		final Chat chat = getChat();
		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case changed:
				if (eventChat.equals(chat)) {
					setData(getData().copyForNewChat(eventChat));
				}
				break;

			case last_message_changed:
				if (eventChat.equals(chat)) {
					setData(getData().copyForNewLastMessage(event.getDataAsChatMessage()));
				}
				break;
			case unread_message_count_changed:
				if (eventChat.equals(chat)) {
					setData(getData().copyForNewUnreadMessageCount(event.getDataAsInteger()));
				}
				break;
		}

	}
}
