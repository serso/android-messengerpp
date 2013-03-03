package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.os.Handler;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;

import java.util.*;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 11:27 PM
 */
public class MessagesAdapter extends MessengerListItemAdapter<MessageListItem> implements ChatEventListener, UserEventListener {

    @Nonnull
    private final User user;

    @Nonnull
    private Chat chat;

    // map of list items saying that someone start typing message
    // key: realm user id
    @Nonnull
    private final Map<RealmEntity, MessageListItem> userTypingListItems = Collections.synchronizedMap(new HashMap<RealmEntity, MessageListItem>());

    public MessagesAdapter(@Nonnull Context context, @Nonnull User user, @Nonnull Chat chat) {
        super(context, new ArrayList<MessageListItem>());
        this.user = user;
        this.chat = chat;
    }

    @Override
    public void onChatEvent(@Nonnull Chat eventChat, @Nonnull ChatEventType chatEventType, @Nullable Object data) {

        if (chatEventType == ChatEventType.message_removed) {
            if (eventChat.equals(chat)) {
                final String messageId = (String) data;
                assert messageId != null;
                removeMessageListItem(messageId);
            }
        }

        if (chatEventType == ChatEventType.message_added) {
            if (eventChat.equals(chat)) {
                final ChatMessage message = (ChatMessage) data;
                assert message != null;
                addMessageListItem(message);
            }
        }

        if (chatEventType == ChatEventType.message_added_batch) {
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
                    final MessageListItem listItem = userTypingListItems.remove(message.getAuthor().getRealmUser());
                    if ( listItem != null ) {
                        removeListItem(listItem);
                    }
                }
            }
        }

        if (chatEventType == ChatEventType.message_changed) {
            if (eventChat.equals(chat)) {
                final ChatMessage message = (ChatMessage) data;
                final MessageListItem listItem = findInAllElements(message);
                if (listItem != null) {
                    listItem.onChatEvent(eventChat, chatEventType, data);
                }
            }

            //notifyDataSetChanged();
        }

        if (chatEventType.isEvent(ChatEventType.user_start_typing, eventChat, chat)) {
            final RealmEntity realmUser = (RealmEntity) data;

            if (!userTypingListItems.containsKey(realmUser)) {
                assert realmUser != null;

                final LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance("typing" + realmUser.getEntityId());
                liteChatMessage.setSendDate(DateTime.now());
                liteChatMessage.setAuthor(MessengerApplication.getServiceLocator().getUserService().getUserById(realmUser));
                liteChatMessage.setBody("User start typing...");

                final MessageListItem listItem = createListItem(ChatMessageImpl.newInstance(liteChatMessage));
                addListItem(listItem);
                userTypingListItems.put(realmUser, listItem);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeListItem(listItem);
                        userTypingListItems.remove(realmUser);
                    }
                }, 3000);
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
        userTypingListItems.remove(message.getAuthor().getRealmUser());

        addListItem(createListItem(message));
    }

    protected void removeListItem(@Nonnull ChatMessage message) {
        remove(createListItem(message));
    }

    private void removeMessageListItem(@Nonnull String messageId) {
        // todo serso: not good solution => better way is to load full message object (but it can take long time)
        final ChatMessage message = ChatMessageImpl.newInstance(LiteChatMessageImpl.newInstance(messageId));
        removeListItem(message);
    }
}
