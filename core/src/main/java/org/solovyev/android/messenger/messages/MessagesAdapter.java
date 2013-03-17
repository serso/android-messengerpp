package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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

    // map of list items saying that someone start typing message
    // key: user entity
    @Nonnull
    private final Map<Entity, MessageListItem> userTypingListItems = Collections.synchronizedMap(new HashMap<Entity, MessageListItem>());

    @Nonnull
    private final Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@Nonnull Message msg) {
            switch (msg.what) {
                case REMOVE_USER_START_TYPING_ID:
                    final MessageListItem listItem = (MessageListItem) msg.obj;
                    removeListItem(listItem);
                    userTypingListItems.remove(listItem.getMessage().getAuthor().getEntity());
                    return true;

            }
            return false;
        }
    });

    public MessagesAdapter(@Nonnull Context context, @Nonnull User user, @Nonnull Chat chat) {
        super(context, new ArrayList<MessageListItem>());
        this.user = user;
        this.chat = chat;
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
                final ChatMessage message = (ChatMessage) data;
                assert message != null;
                addMessageListItem(message);
            }
        }

        if (type == ChatEventType.message_added_batch) {
            if (eventChat.equals(chat)) {
                final List<ChatMessage> messages = (List<ChatMessage>) data;
                assert messages != null;

                addListItems(Lists.transform(messages, new Function<ChatMessage, MessageListItem>() {
                    @Override
                    public MessageListItem apply(@javax.annotation.Nullable ChatMessage input) {
                        assert input != null;
                        return createListItem(input);
                    }
                }));

                for (ChatMessage message : messages) {
                    final MessageListItem listItem = userTypingListItems.remove(message.getAuthor().getEntity());
                    if ( listItem != null ) {
                        removeListItem(listItem);
                    }
                }
            }
        }

        if (type == ChatEventType.message_changed) {
            if (eventChat.equals(chat)) {
                final ChatMessage message = (ChatMessage) data;
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
                onTypingEvent(type, user);
            }
        }
    }

    private void onTypingEvent(@Nonnull ChatEventType type, @Nonnull Entity user) {
        MessageListItem listItem = userTypingListItems.get(user);
        if (type == ChatEventType.user_starts_typing) {
            if (listItem == null) {
                // 'Typing' message is not shown yet => show it

                // create fake message
                final LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance(EntityImpl.fromEntityId(user.getEntityId() + "_typing"));
                liteChatMessage.setSendDate(DateTime.now());
                liteChatMessage.setAuthor(MessengerApplication.getServiceLocator().getUserService().getUserById(user));
                liteChatMessage.setBody(getContext().getString(R.string.mpp_user_starts_typing));

                // create fake list item
                listItem = createListItem(ChatMessageImpl.newInstance(liteChatMessage));
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
    private MessageListItem findInAllElements(@Nonnull ChatMessage message) {
        return Iterables.find(getAllElements(), Predicates.<MessageListItem>equalTo(createListItem(message)), null);
    }

    @Nonnull
    private MessageListItem createListItem(@Nonnull ChatMessage message) {
        return new MessageListItem(user, chat, message);
    }

    private void addMessageListItem(@Nonnull ChatMessage message) {
        // remove typing message
        userTypingListItems.remove(message.getAuthor().getEntity());

        addListItem(createListItem(message));
    }

    protected void removeListItem(@Nonnull ChatMessage message) {
        remove(createListItem(message));
    }

    private void removeMessageListItem(@Nonnull String messageId) {
        final ChatMessage message = ChatMessageImpl.newInstance(ChatMessages.newEmptyMessage(messageId));
        removeListItem(message);
    }
}
