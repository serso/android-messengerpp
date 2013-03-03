package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

final class UserContact {

    @Nonnull
    private final User user;

    @Nonnull
    private final User contact;

    UserContact(@Nonnull User user, @Nonnull User contact) {
        this.user = user;
        this.contact = contact;
    }

    @Nonnull
    static UserContact newInstance(@Nonnull User user, @Nonnull User contact) {
        return new UserContact(user, contact);
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nonnull
    public User getContact() {
        return contact;
    }
}
