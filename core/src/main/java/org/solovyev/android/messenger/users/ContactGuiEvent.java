package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public class ContactGuiEvent {

    @NotNull
    private final ContactGuiEventType type;

    @NotNull
    private final User contact;

    public ContactGuiEvent(@NotNull ContactGuiEventType type,
                           @NotNull User contact) {
        this.contact = contact;
        this.type = type;
    }

    @NotNull
    public ContactGuiEventType getType() {
        return type;
    }

    @NotNull
    public User getContact() {
        return contact;
    }
}
