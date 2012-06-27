package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.VersionedEntityImpl;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractFriendsAdapter extends AbstractMessengerListItemAdapter implements UserEventListener {

    public AbstractFriendsAdapter(@NotNull Context context, @NotNull User user) {
        super(context, new ArrayList<ListItem<? extends View>>(), user);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if (userEventType == UserEventType.friend_removed) {
            if (eventUser.equals(getUser())) {
                final Integer friendId = (Integer) data;
                removeListItem(eventUser, friendId);
            }
        }

        if (userEventType == UserEventType.friend_added) {
            if (eventUser.equals(getUser())) {
                final User friend = (User) data;
                if (canAddFriend(friend)) {
                    addListItem(eventUser, friend);
                }
            }
        }

        if (userEventType == UserEventType.friend_added_batch) {
            if (eventUser.equals(getUser())) {
                // first - filter friends which can be added
                // then - transform user objects to list items objects
                addListItems(Lists.newArrayList(Iterables.transform(Iterables.filter((List<User>) data, new Predicate<User>() {
                    @Override
                    public boolean apply(@javax.annotation.Nullable User friends) {
                        assert friends != null;
                        return canAddFriend(friends);
                    }
                }), new Function<User, ListItem<?>>() {
                    @Override
                    public ListItem<?> apply(@javax.annotation.Nullable User friend) {
                        return createListItem(eventUser, friend);
                    }
                })));
            }
        }

        if (userEventType == UserEventType.changed) {

            final ListItem<?> listItem = findInAllElements(getUser(), eventUser);
            if (listItem instanceof UserEventListener) {
                ((UserEventListener) listItem).onUserEvent(eventUser, userEventType, data);
                onListItemChanged(getUser(), eventUser);
            }
            //notifyDataSetChanged();
        }
    }

    @Nullable
    protected ListItem<?> findInAllElements(@NotNull User user, @NotNull User friend) {
        return Iterables.find(getAllElements(), Predicates.<ListItem<?>>equalTo(createListItem(user, friend)), null);
    }


    protected void removeListItem(@NotNull User user, @NotNull Integer friendId) {
        // todo serso: not good solution => better way is to load full user object for friend (but it can take long time)
        final User friend = UserImpl.newInstance(new VersionedEntityImpl(friendId));
        removeListItem(user, friend);
    }

    protected void removeListItem(@NotNull User user, @NotNull User friend) {
        remove(createListItem(user, friend));
    }

    protected void addListItem(@NotNull User user, @NotNull User friend) {
        addListItem(createListItem(user, friend));
    }

    @NotNull
    private FriendListItem createListItem(@NotNull User user, @NotNull User friend) {
        return new FriendListItem(user, friend);
    }

    protected abstract void onListItemChanged(@NotNull User user, @NotNull User friend);

    protected abstract boolean canAddFriend(@NotNull User friend);
}
