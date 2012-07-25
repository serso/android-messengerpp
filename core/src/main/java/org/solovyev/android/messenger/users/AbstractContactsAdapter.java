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
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractContactsAdapter extends AbstractMessengerListItemAdapter implements UserEventListener {

    public AbstractContactsAdapter(@NotNull Context context, @NotNull User user) {
        super(context, new ArrayList<ListItem<? extends View>>(), user);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if (userEventType == UserEventType.contact_removed) {
            if (eventUser.equals(getUser())) {
                final String contactId = (String) data;
                removeListItem(eventUser, contactId);
            }
        }

        if (userEventType == UserEventType.contact_added) {
            if (eventUser.equals(getUser())) {
                final User contact = (User) data;
                if (canAddContact(contact)) {
                    addListItem(eventUser, contact);
                }
            }
        }

        if (userEventType == UserEventType.contact_added_batch) {
            if (eventUser.equals(getUser())) {
                // first - filter contacts which can be added
                // then - transform user objects to list items objects
                addListItems(Lists.newArrayList(Iterables.transform(Iterables.filter((List<User>) data, new Predicate<User>() {
                    @Override
                    public boolean apply(@javax.annotation.Nullable User contacts) {
                        assert contacts != null;
                        return canAddContact(contacts);
                    }
                }), new Function<User, ListItem<?>>() {
                    @Override
                    public ListItem<?> apply(@javax.annotation.Nullable User contact) {
                        return createListItem(eventUser, contact);
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
    protected ListItem<?> findInAllElements(@NotNull User user, @NotNull User contact) {
        return Iterables.find(getAllElements(), Predicates.<ListItem<?>>equalTo(createListItem(user, contact)), null);
    }


    protected void removeListItem(@NotNull User user, @NotNull String contactId) {
        // todo serso: not good solution => better way is to load full user object for contact (but it can take long time)
        final User contact = UserImpl.newInstance(contactId);
        removeListItem(user, contact);
    }

    protected void removeListItem(@NotNull User user, @NotNull User contact) {
        remove(createListItem(user, contact));
    }

    protected void addListItem(@NotNull User user, @NotNull User contact) {
        addListItem(createListItem(user, contact));
    }

    @NotNull
    private ContactListItem createListItem(@NotNull User user, @NotNull User contact) {
        return new ContactListItem(user, contact);
    }

    protected abstract void onListItemChanged(@NotNull User user, @NotNull User contact);

    protected abstract boolean canAddContact(@NotNull User contact);
}
