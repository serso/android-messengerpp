package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public class ContactGuiEvent {

    @Nonnull
    private final ContactGuiEventType type;

    @Nonnull
    private final User contact;

    public ContactGuiEvent(@Nonnull ContactGuiEventType type,
                           @Nonnull User contact) {
        this.contact = contact;
        this.type = type;
    }

    @Nonnull
    public ContactGuiEventType getType() {
        return type;
    }

    @Nonnull
    public User getContact() {
        return contact;
    }
}
