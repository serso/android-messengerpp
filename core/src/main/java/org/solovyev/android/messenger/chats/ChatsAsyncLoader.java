package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.users.User;

import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:23 PM
 */
public class ChatsAsyncLoader extends AbstractAsyncLoader<Chat> {

    ChatsAsyncLoader(@NotNull User user, @NotNull Context context, @NotNull ListItemArrayAdapter adapter, @Nullable Runnable onPostExecute) {
        super(user, context, adapter, onPostExecute);
    }

    @NotNull
    @Override
    protected List<Chat> getElements(@NotNull Context context) {
        return getServiceLocator().getUserService().getUserChats(getUser().getId(), context);
    }

    @Override
    protected Comparator<? super ListItem<? extends View>> getComparator() {
        return ChatListItem.Comparator.getInstance();
    }

    @NotNull
    @Override
    protected ListItem<?> createListItem(@NotNull Chat chat) {
        return new ChatListItem(getUser(), chat, getContext());
    }
}
