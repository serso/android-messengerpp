package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;

final class UserContact {

    @NotNull
    private final User user;

    @NotNull
    private final User contact;

    UserContact(@NotNull User user, @NotNull User contact) {
        this.user = user;
        this.contact = contact;
    }

    @NotNull
    static UserContact newInstance(@NotNull User user, @NotNull User contact) {
        return new UserContact(user, contact);
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public User getContact() {
        return contact;
    }
}
