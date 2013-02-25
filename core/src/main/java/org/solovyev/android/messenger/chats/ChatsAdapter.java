package org.solovyev.android.messenger.chats;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;
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
public class ChatsAdapter extends AbstractMessengerListItemAdapter<ChatListItem> implements ChatEventListener, UserEventListener {

    public ChatsAdapter(@NotNull Context context, @NotNull User user) {
        super(context, new ArrayList<ChatListItem>(), user);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
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

    protected void removeListItem(@NotNull User user, @NotNull String chatId) {
        // todo serso: not good solution => better way is to load full user object for chat (but it can take long time)
        final Chat chat = ChatImpl.newFakeChat(chatId);
        removeListItem(user, chat);
    }

    protected void removeListItem(@NotNull User user, @NotNull Chat chat) {
        remove(createListItem(user, chat));
    }

    protected void addListItem(@NotNull User user, @NotNull Chat chat) {
        addListItem(createListItem(user, chat));
    }

    @NotNull
    private ChatListItem createListItem(@NotNull User user, @NotNull Chat chat) {
        return new ChatListItem(user, chat, getContext());
    }

    @Override
    protected Comparator<? super ChatListItem> getComparator() {
        return ChatListItem.Comparator.getInstance();
    }

    @NotNull
    private ChatService getChatService() {
        return AbstractMessengerApplication.getServiceLocator().getChatService();
    }

    @Override
    public void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data) {

        if (chatEventType == ChatEventType.changed || chatEventType == ChatEventType.last_message_changed) {
            final ChatListItem chatListItem = findInAllElements(getUser(), eventChat);
            if (chatListItem != null) {
                chatListItem.onChatEvent(eventChat, chatEventType, data);
            }
        }
    }

    @Nullable
    protected ChatListItem findInAllElements(@NotNull User user, @NotNull Chat chat) {
        return Iterables.find(getAllElements(), Predicates.<ChatListItem>equalTo(createListItem(user, chat)), null);
    }

}
