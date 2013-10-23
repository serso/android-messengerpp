package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.Messages.newMessage;

public class MessagesAdapter extends BaseListItemAdapter<MessageListItem> /*implements ChatEventListener, UserEventListener*/ {

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

	@Nonnull
	private static final String TAG = newTag("MessagesAdapter");

	/*private*/ static final String TYPING_POSTFIX = "_typing";

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

	// as id might be changed in service let's use something more consistent, e.g. date as a key
	// key: sending time, value; sending message
	@Nonnull
	private final Map<DateTime, MessageListItem> sendingListItems = new HashMap<DateTime, MessageListItem>();

	@Nonnull
	private final Handler uiHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(@Nonnull android.os.Message msg) {
			switch (msg.what) {
				case REMOVE_USER_START_TYPING_ID:
					final MessageListItem listItem = (MessageListItem) msg.obj;
					while (findInAllElements(listItem) != null) {
						remove(listItem);
					}
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
		Log.d(TAG, "On event: " + event);
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
				case user_is_typing:
				case user_is_not_typing:
					onTypingEvent(type, event.getDataAsEntity(), chat);
					break;
			}
		}
	}

	private void onMessageChanged(@Nonnull ChatEvent event) {
		final Message message = event.getDataAsMessage();
		final MessageListItem listItem = findInAllElements(message);
		if (listItem != null) {
			listItem.onMessageChanged(event.getDataAsMessage());
			notifyDataSetChanged();
		}
	}

	void addSendingMessage(@Nonnull Message message) {
		final MessageListItem listItem = newMessageListItem(message);
		add(listItem);
		sendingListItems.put(message.getSendDate(), listItem);
	}

	private void addMessages(@Nonnull List<Message> messages) {
		final List<MessageListItem> listItems = transform(messages, new Function<Message, MessageListItem>() {
			@Override
			public MessageListItem apply(Message message) {
				return newMessageListItem(message);
			}
		});

		for (MessageListItem listItem : listItems) {
			final Message message = listItem.getMessage();
			removeTypingListItem(message);
			removeSendingListItem(message);
		}

		addAll(listItems);
	}

	private void removeTypingListItem(@Nonnull Message message) {
		if (message.isIncoming()) {
			final MessageListItem typingListItem = userTypingListItems.remove(message.getAuthor());
			if (typingListItem != null) {
				remove(typingListItem);
			}
		}
	}

	private void removeSendingListItem(Message message) {
		if (message.isOutgoing()) {
			final MessageListItem sendingListItem = sendingListItems.remove(message.getSendDate());
			if(sendingListItem != null) {
				remove(sendingListItem);
			}
		}
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
		if (type == ChatEventType.user_is_typing) {
			if (listItem == null) {
				// 'Typing' message is not shown yet => show it

				// create fake message
				final MutableMessage message = newTypingMessage(user, chat);

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

	@Nonnull
	MutableMessage newTypingMessage(@Nonnull Entity user, @Nonnull Chat chat) {
		final MutableMessage message = newMessage(newEntityFromEntityId(user.getEntityId() + TYPING_POSTFIX));
		message.setChat(chat.getEntity());
		message.setSendDate(DateTime.now());
		message.setAuthor(user);
		message.setBody(getTypingMessageBody());
		message.setRead(true);
		return message;
	}

	@Nonnull
	String getTypingMessageBody() {
		return getContext().getString(R.string.mpp_user_is_typing);
	}

	@Override
	protected Comparator<? super MessageListItem> getComparator() {
		return MessageListItem.Comparator.getInstance();
	}

	@Nullable
	private MessageListItem findInAllElements(@Nonnull Message message) {
		return findInAllElements(newMessageListItem(message));
	}

	@Nullable
	private MessageListItem findInAllElements(@Nonnull MessageListItem listItem) {
		return find(getAllElements(), Predicates.<MessageListItem>equalTo(listItem), null);
	}

	@Nonnull
	private MessageListItem newMessageListItem(@Nonnull Message message) {
		return MessageListItem.newMessageListItem(user, chat, message, messageStyle);
	}

	protected void removeListItem(@Nonnull Message message) {
		remove(newMessageListItem(message));
	}
}
