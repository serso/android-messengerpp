package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.events.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public class ContactGuiEvent extends AbstractTypedJEvent<User, ContactGuiEventType> {

    public ContactGuiEvent(@Nonnull User contact, @Nonnull ContactGuiEventType type) {
        super(contact, type, null);
    }

    @Nonnull
    public User getContact() {
        return getEventObject();
    }
}
