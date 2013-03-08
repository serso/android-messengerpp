package org.solovyev.android.messenger.users;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.fragments.AbstractFragmentReuseCondition;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;

/**
* User: serso
* Date: 3/5/13
* Time: 1:57 PM
*/
public final class ContactFragmentReuseCondition extends AbstractFragmentReuseCondition<MessengerContactFragment> {

    @Nonnull
    private final RealmEntity contact;

    public ContactFragmentReuseCondition(@Nonnull RealmEntity contact) {
        super(MessengerContactFragment.class);
        this.contact = contact;
    }

    @Nonnull
    public static JPredicate<Fragment> forContact(@Nonnull RealmEntity contact) {
        return new ContactFragmentReuseCondition(contact);
    }

    @Override
    protected boolean canReuseFragment(@Nonnull MessengerContactFragment fragment) {
        return contact.equals(fragment.getContact());
    }
}
