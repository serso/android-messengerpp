package org.solovyev.android.messenger.users;

import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;

import org.solovyev.android.fragments.AbstractFragmentReuseCondition;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.common.JPredicate;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:57 PM
 */
public final class ContactFragmentReuseCondition extends AbstractFragmentReuseCondition<ContactFragment> {

	@Nonnull
	private final Entity contact;

	public ContactFragmentReuseCondition(@Nonnull Entity contact) {
		super(ContactFragment.class);
		this.contact = contact;
	}

	@Nonnull
	public static JPredicate<Fragment> forContact(@Nonnull Entity contact) {
		return new ContactFragmentReuseCondition(contact);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull ContactFragment fragment) {
		return contact.equals(fragment.getContact());
	}
}
