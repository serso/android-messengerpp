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
    protected void onListItemChanged(@Nonnull User user, @Nonnull User contact) {
        if ( !contact.isOnline() ) {
            removeListItem(user, contact);
        }
    }

    @Override
    protected boolean canAddContact(@Nonnull User contact) {
        return contact.isOnline();
    }
}
