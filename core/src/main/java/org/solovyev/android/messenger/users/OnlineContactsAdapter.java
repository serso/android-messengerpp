package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.messenger.realms.RealmService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 6:01 PM
 */
public class OnlineContactsAdapter extends AbstractContactsAdapter {

    public OnlineContactsAdapter(@Nonnull Context context, @Nonnull RealmService realmService) {
        super(context, realmService);
    }

    @Override
    public void onEvent(@Nonnull UserEvent event) {
        super.onEvent(event);

        if (event.isOfType(UserEventType.contact_offline)) {
            final User offlineContact = event.getDataAsUser();
            removeListItem(offlineContact);

        }

        if (event.isOfType(UserEventType.contact_online)) {
            final User onlineContact = event.getDataAsUser();
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
