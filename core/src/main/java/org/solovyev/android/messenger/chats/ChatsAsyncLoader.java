package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;

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
public class ChatsAsyncLoader extends AbstractAsyncLoader<UserChat, ChatListItem> {

    @Nonnull
    private final RealmService realmService;

    ChatsAsyncLoader(@Nonnull Context context, @Nonnull ListItemArrayAdapter<ChatListItem> adapter, @Nullable Runnable onPostExecute, @Nonnull RealmService realmService) {
        super(context, adapter, onPostExecute);
        this.realmService = realmService;
    }

    @Nonnull
    @Override
    protected List<UserChat> getElements(@Nonnull Context context) {
        final List<UserChat> result = new ArrayList<UserChat>();

        for (Realm realm : realmService.getRealms()) {
            final User user = realm.getUser();
            for (Chat chat : MessengerApplication.getServiceLocator().getUserService().getUserChats(user.getEntity())) {
                result.add(UserChat.newInstance(user, chat, null));
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
    protected ChatListItem createListItem(@Nonnull UserChat userChat) {
        return new ChatListItem(userChat.getUser(), userChat.getChat(), getContext());
    }

}
