package org.solovyev.android.messenger.chats;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:48 PM
 */
public class ChatsAdapter extends MessengerListItemAdapter<ChatListItem> implements ChatEventListener, UserEventListener {

    public ChatsAdapter(@Nonnull Context context) {
        super(context, new ArrayList<ChatListItem>());
    }

    @Override
    public void onUserEvent(@Nonnull final User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if (userEventType == UserEventType.chat_removed) {
            if (eventUser.equals(getUser())) {
                final String chatId = (String) data;
                removeListItem(eventUser, chatId);
            }
        }

        if (userEventType == UserEventType.chat_added) {
            if (eventUser.equals(getUser())) {
                final Chat chat = (Chat) data;
                addListItem(eventUser, chat);
            }
        }

        if (userEventType == UserEventType.chat_added_batch) {
            if (eventUser.equals(getUser())) {
                final List<Chat> chats = (List<Chat>) data;
                addListItems(Lists.transform(chats, new Function<Chat, ChatListItem>() {
                    @Override
                    public ChatListItem apply(@javax.annotation.Nullable Chat chat) {
                        assert chat != null;
                        return createListItem(eventUser, chat);
                    }
                }));
            }
        }
    }

    private User getUser() {
        // todo serso: continue
        throw new UnsupportedOperationException();
    }

    protected void removeListItem(@Nonnull User user, @Nonnull String chatId) {
        // todo serso: not good solution => better way is to load full user object for chat (but it can take long time)
        final Chat chat = ChatImpl.newFakeChat(chatId);
        removeListItem(user, chat);
    }

    protected void removeListItem(@Nonnull User user, @Nonnull Chat chat) {
        remove(createListItem(user, chat));
    }

    protected void addListItem(@Nonnull User user, @Nonnull Chat chat) {
        addListItem(createListItem(user, chat));
    }

    @Nonnull
    private ChatListItem createListItem(@Nonnull User user, @Nonnull Chat chat) {
        return new ChatListItem(user, chat, getContext());
    }

    @Override
    protected Comparator<? super ChatListItem> getComparator() {
        return ChatListItem.Comparator.getInstance();
    }

    @Nonnull
    private ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @Override
    public void onChatEvent(@Nonnull Chat eventChat, @Nonnull ChatEventType chatEventType, @Nullable Object data) {

        if (chatEventType == ChatEventType.changed || chatEventType == ChatEventType.last_message_changed) {
            final ChatListItem chatListItem = findInAllElements(getUser(), eventChat);
            if (chatListItem != null) {
                chatListItem.onChatEvent(eventChat, chatEventType, data);
            }
        }
    }

    @Nullable
    protected ChatListItem findInAllElements(@Nonnull User user, @Nonnull Chat chat) {
        return Iterables.find(getAllElements(), Predicates.<ChatListItem>equalTo(createListItem(user, chat)), null);
    }

}
