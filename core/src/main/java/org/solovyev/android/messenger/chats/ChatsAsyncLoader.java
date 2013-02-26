package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:23 PM
 */
public class ChatsAsyncLoader extends AbstractAsyncLoader<UserChat, ChatListItem> {

    @NotNull
    private final RealmService realmService;

    ChatsAsyncLoader(@NotNull Context context, @NotNull ListItemArrayAdapter<ChatListItem> adapter, @Nullable Runnable onPostExecute, @NotNull RealmService realmService) {
        super(context, adapter, onPostExecute);
        this.realmService = realmService;
    }

    @NotNull
    @Override
    protected List<UserChat> getElements(@NotNull Context context) {
        final List<UserChat> result = new ArrayList<UserChat>();

        for (Realm realm : realmService.getRealms()) {
            final User user = realm.getUser();
            for (Chat chat : MessengerApplication.getServiceLocator().getUserService().getUserChats(user.getRealmUser())) {
                result.add(new UserChat(user, chat));
            }
        }

        return result;
    }

    @Override
    protected Comparator<? super ChatListItem> getComparator() {
        return ChatListItem.Comparator.getInstance();
    }

    @NotNull
    @Override
    protected ChatListItem createListItem(@NotNull UserChat userChat) {
        return new ChatListItem(userChat.getUser(), userChat.getChat(), getContext());
    }

}
