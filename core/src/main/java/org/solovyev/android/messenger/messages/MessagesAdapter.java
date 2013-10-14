package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Handler;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.MessageListItem.newMessageListItem;
import static org.solovyev.android.messenger.messages.Messages.newEmptyMessage;
import static org.solovyev.android.messenger.messages.Messages.newMessage;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 11:27 PM
 */
public class MessagesAdapter extends MessengerListItemAdapter<MessageListItem> /*implements ChatEventListener, UserEventListener*/ {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final int REMOVE_USER_START_TYPING_ID = 1;

	// todo serso: here we need to use different values for different realms
	// for example, XMPP realms sends 'Stop typing' event => this constant must be high enough
	// VK doesn't send 'Stop typing' => this constant must be low enough
	private static final int REMOVE_USER_START_TYPING_DELAY = 30000;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final User user;

	@Nonnull
	private Chat chat;

	@Nonnull
	private final MessageListItemStyle messageStyle;

	// map of list items saying that someone start typing message
	// key: user entity
	@Nonnull
	private final Map<Entity, MessageListItem> userTypingListItems = Collections.synchronizedMap(new HashMap<Entity, MessageListItem>());

	@Nonnull
	private final Handler uiHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(@Nonnull android.os.Message msg) {
			switch (msg.what) {
				case REMOVE_USER_START_TYPING_ID:
					final MessageListItem listItem = (MessageListItem) msg.obj;
					removeListItem(listItem);
					userTypingListItems.remove(listItem.getMessage().getAuthor());
					return true;

			}
			return false;
		}
	});

	public MessagesAdapter(@Nonnull Context context, @Nonnull User user, @Nonnull Chat chat, @Nonnull MessageListItemStyle messageStyle) {
		super(context, new ArrayList<MessageListItem>(), false);
		this.user = user;
		this.chat = chat;
		this.messageStyle = messageStyle;
	}

	/*@Override*/
	public void onEvent(@Nonnull ChatEvent event) {
		final ChatEventType type = event.getType();
		final Chat eventChat = event.getChat();
		final Object data = event.getData();

		if (type == ChatEventType.message_removed) {
			if (eventChat.equals(chat)) {
				final String messageId = (String) data;
				assert messageId != null;
				removeMessageListItem(messageId);
			}
		}

		if (type == ChatEventType.message_added) {
			if (eventChat.equals(chat)) {
				addMessageListItem(event.getDataAsMessage());
			}
		}

		if (type == ChatEventType.message_added_batch) {
			if (eventChat.equals(chat)) {
				final List<Message> messages = event.getDataAsMessages();

				addListItems(Lists.transform(messages, new Function<Message, MessageListItem>() {
					@Override
					public MessageListItem apply(@javax.annotation.Nullable Message input) {
						assert input != null;
						return createListItem(input);
					}
				}));

				for (Message message : messages) {
					final MessageListItem listItem = userTypingListItems.remove(message.getAuthor());
					if (listItem != null) {
						removeListItem(listItem);
					}
				}
			}
		}

		if (type == ChatEventType.message_changed) {
			if (eventChat.equals(chat)) {
				final Message message = (Message) data;
				final MessageListItem listItem = findInAllElements(message);
				if (listItem != null) {
					listItem.onEvent(event);
				}
			}
		}

		if (event.isOfType(ChatEventType.user_starts_typing, ChatEventType.user_stops_typing)) {
			if (eventChat.equals(chat)) {
				final Entity user = (Entity) data;
				assert user != null;
				onTypingEvent(type, user, chat);
			}
		}
	}

	private void onTypingEvent(@Nonnull ChatEventType type, @Nonnull Entity user, @Nonnull Chat chat) {
		MessageListItem listItem = userTypingListItems.get(user);
		if (type == ChatEventType.user_starts_typing) {
			if (listItem == null) {
				// 'Typing' message is not shown yet => show it

				// create fake message
				final MutableMessage message = newMessage(newEntityFromEntityId(user.getEntityId() + "_typing"));
				message.setChat(chat.getEntity());
				message.setSendDate(DateTime.now());
				message.setAuthor(user);
				message.setBody(getContext().getString(R.string.mpp_user_is_typing));
				message.setRead(true);

				// create fake list item
				listItem = createListItem(message);
				addListItem(listItem);

				// add list item to the map
				userTypingListItems.put(user, listItem);

				// send DELAYED 'Removal' message
				uiHandler.sendMessageDelayed(uiHandler.obtainMessage(REMOVE_USER_START_TYPING_ID, listItem), REMOVE_USER_START_TYPING_DELAY);
			} else {
				// 'Typing' message is already shown => prolong the time

				// remove old 'Removal' message
				uiHandler.removeMessages(REMOVE_USER_START_TYPING_ID);

				// add new 'Removal' message
				uiHandler.sendMessageDelayed(uiHandler.obtainMessage(REMOVE_USER_START_TYPING_ID, listItem), REMOVE_USER_START_TYPING_DELAY);
			}
		} else {
			if (listItem != null) {
				// message is still shown
				uiHandler.removeMessages(REMOVE_USER_START_TYPING_ID);
				uiHandler.sendMessage(uiHandler.obtainMessage(REMOVE_USER_START_TYPING_ID, listItem));
			} else {
				// message is not shown => no removal is needed
			}
		}
	}

	@Override
	protected Comparator<? super MessageListItem> getComparator() {
		return MessageListItem.Comparator.getInstance();
	}

	@Nullable
	private MessageListItem findInAllElements(@Nonnull Message message) {
		return Iterables.find(getAllElements(), Predicates.<MessageListItem>equalTo(createListItem(message)), null);
	}

	@Nonnull
	private MessageListItem createListItem(@Nonnull Message message) {
		return newMessageListItem(user, chat, message, messageStyle);
	}

	private void addMessageListItem(@Nonnull Message message) {
		// remove typing message
		userTypingListItems.remove(message.getAuthor());

		addListItem(createListItem(message));
	}

	protected void removeListItem(@Nonnull Message message) {
		remove(createListItem(message));
	}

	private void removeMessageListItem(@Nonnull String messageId) {
		removeListItem(newEmptyMessage(messageId));
	}
}
