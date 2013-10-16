package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
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
	private final Map<Entity, MessageListItem> userTypingListItems = new HashMap<Entity, MessageListItem>();

	@Nonnull
	private final Set<MessageListItem> sendingListItems = new HashSet<MessageListItem>();

	@Nonnull
	private final Handler uiHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(@Nonnull android.os.Message msg) {
			switch (msg.what) {
				case REMOVE_USER_START_TYPING_ID:
					final MessageListItem listItem = (MessageListItem) msg.obj;
					remove(listItem);
					userTypingListItems.remove(listItem.getMessage().getAuthor());
					return true;

			}
			return false;
		}
	});

	public MessagesAdapter(@Nonnull Context context, @Nonnull User user, @Nonnull Chat chat, @Nonnull MessageListItemStyle messageStyle) {
		super(context, new ArrayList<MessageListItem>(), false, false);
		this.user = user;
		this.chat = chat;
		this.messageStyle = messageStyle;
	}

	/*@Override*/
	public void onEvent(@Nonnull ChatEvent event) {
		final ChatEventType type = event.getType();

		if (event.getChat().equals(chat)) {
			switch (type) {
				case message_added:
					addMessages(asList(event.getDataAsMessage()));
					break;
				case message_added_batch:
					addMessages(event.getDataAsMessages());
					break;
				case message_state_changed:
					onMessageStateChanged(event);
					break;
				case message_changed:
					onMessageChanged(event);
					break;
				case user_starts_typing:
				case user_stops_typing:
					onTypingEvent(type, event.getDataAsEntity(), chat);
					break;
			}
		}
	}

	private void onMessageChanged(@Nonnull ChatEvent event) {
		final MessageListItem listItem = findInAllElements(event.getDataAsMessage());
		if (listItem != null) {
			listItem.onMessageChanged(event.getDataAsMessage());
			notifyDataSetChanged();
		}
	}

	public void addSendingMessage(@Nonnull Message message) {
		final MessageListItem listItem = newMessageListItem(message);
		add(listItem);
		sendingListItems.add(listItem);
	}

	private void addMessages(@Nonnull List<Message> messages) {
		final List<MessageListItem> listItems = transform(messages, new Function<Message, MessageListItem>() {
			@Override
			public MessageListItem apply(Message message) {
				return newMessageListItem(message);
			}
		});

		for (MessageListItem listItem : listItems) {
			if (userTypingListItems.remove(listItem.getMessage().getAuthor()) != null) {
				remove(listItem);
			}

			if(sendingListItems.remove(listItem)) {
				remove(listItem);
			}
		}

		addAll(listItems);
	}

	private void onMessageStateChanged(@Nonnull ChatEvent event) {
		onMessageStateChanged(event.getDataAsMessage());
	}

	private void onMessageStateChanged(@Nonnull Message message) {
		switch (message.getState()) {
			case removed:
				removeListItem(message);
				break;
			default:
				final MessageListItem listItem = findInAllElements(message);
				if(listItem != null) {
					listItem.onMessageChanged(message);
					notifyDataSetChanged();
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
				listItem = newMessageListItem(message);
				add(listItem);

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
		return Iterables.find(getAllElements(), Predicates.<MessageListItem>equalTo(newMessageListItem(message)), null);
	}

	@Nonnull
	private MessageListItem newMessageListItem(@Nonnull Message message) {
		return MessageListItem.newMessageListItem(user, chat, message, messageStyle);
	}

	protected void removeListItem(@Nonnull Message message) {
		remove(newMessageListItem(message));
	}
}
