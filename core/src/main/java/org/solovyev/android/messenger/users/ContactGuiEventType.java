package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactGuiEventType {

    contact_clicked;

    @NotNull
    public static ContactGuiEvent newContactClicked(@NotNull User contact) {
        return new ContactGuiEvent(contact_clicked, contact);
    }
}
