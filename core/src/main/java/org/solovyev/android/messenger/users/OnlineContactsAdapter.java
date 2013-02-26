package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 6:01 PM
 */
public class OnlineContactsAdapter extends AbstractContactsAdapter {

    public OnlineContactsAdapter(@NotNull Context context) {
        super(context);
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if ( userEventType == UserEventType.contact_offline) {
            if ( eventUser.equals(getUser()) ) {
                final User offlineContact = (User)data;
                removeListItem(eventUser, offlineContact);
            }
        }

        if ( userEventType == UserEventType.contact_online) {
            if ( eventUser.equals(getUser()) ) {
                final User onlineContact = (User)data;
                final ContactListItem listItem = findInAllElements(eventUser, onlineContact);
                if ( listItem == null ) {
                    addListItem(eventUser, onlineContact);
                }
            }
        }
    }

    @Override
    protected void onListItemChanged(@NotNull User user, @NotNull User contact) {
        if ( !contact.isOnline() ) {
            removeListItem(user, contact);
        }
    }

    @Override
    protected boolean canAddContact(@NotNull User contact) {
        return contact.isOnline();
    }
}
