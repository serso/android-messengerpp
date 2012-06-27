package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 6:01 PM
 */
public class OnlineFriendsAdapter extends AbstractFriendsAdapter {

    public OnlineFriendsAdapter(@NotNull Context context, @NotNull User user) {
        super(context, user);
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if ( userEventType == UserEventType.friend_offline ) {
            if ( eventUser.equals(getUser()) ) {
                final User offlineFriend = (User)data;
                removeListItem(eventUser, offlineFriend);
            }
        }

        if ( userEventType == UserEventType.friend_online ) {
            if ( eventUser.equals(getUser()) ) {
                final User onlineFriend = (User)data;
                final ListItem<?> listItem = findInAllElements(eventUser, onlineFriend);
                if ( listItem == null ) {
                    addListItem(eventUser, onlineFriend);
                }
            }
        }
    }

    @Override
    protected void onListItemChanged(@NotNull User user, @NotNull User friend) {
        if ( !friend.isOnline() ) {
            removeListItem(user, friend);
        }
    }

    @Override
    protected boolean canAddFriend(@NotNull User friend) {
        return friend.isOnline();
    }
}
