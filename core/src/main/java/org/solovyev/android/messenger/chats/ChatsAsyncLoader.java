package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:23 PM
 */
final class ChatsAsyncLoader extends AbstractAsyncLoader<UiChat, ChatListItem> {

    ChatsAsyncLoader(@Nonnull Context context, @Nonnull ListItemArrayAdapter<ChatListItem> adapter, @Nullable Runnable onPostExecute) {
        super(context, adapter, onPostExecute);
    }

    @Nonnull
    @Override
    protected List<UiChat> getElements(@Nonnull Context context) {
        final List<UiChat> result = new ArrayList<UiChat>();

        final UserService userService = MessengerApplication.getServiceLocator().getUserService();
        final ChatService chatService = MessengerApplication.getServiceLocator().getChatService();
        final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();


        for (User user : realmService.getRealmUsers()) {
            final List<Chat> chats = userService.getUserChats(user.getEntity());
            for (Chat chat : chats) {
                result.add(UiChat.newInstance(user, chat, chatService.getLastMessage(chat.getEntity()), chatService.getUnreadMessagesCount(chat.getEntity())));
            }
        }

        return result;
    }

    @Override
    protected Comparator<? super ChatListItem> getComparator() {
        return ChatListItemComparator.getInstance();
    }

    @Nonnull
    @Override
    protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
        return new ChatListItem(uiChat);
    }

}
