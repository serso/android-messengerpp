package org.solovyev.android.messenger.users;

import android.content.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 6:01 PM
 */
public class OnlineContactsAdapter extends AbstractContactsAdapter {

    public OnlineContactsAdapter(@Nonnull Context context) {
        super(context);
    }

    @Override
    public void onUserEvent(@Nonnull User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if (userEventType == UserEventType.contact_offline) {
            final User offlineContact = (User) data;
            removeListItem(offlineContact);

        }

        if (userEventType == UserEventType.contact_online) {
            final User onlineContact = (User) data;
            final ContactListItem listItem = findInAllElements(onlineContact);
            if (listItem == null) {
                addListItem(onlineContact);
            }
        }
    }

    @Override
    protected void onListItemChanged(@Nonnull User contact) {
        if (!contact.isOnline()) {
            removeListItem(contact);
        }
    }

    @Override
    protected boolean canAddContact(@Nonnull User contact) {
        return contact.isOnline();
    }
}
