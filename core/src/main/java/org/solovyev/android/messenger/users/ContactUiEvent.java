package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public class ContactUiEvent extends AbstractTypedJEvent<User, ContactUiEventType> {

	public ContactUiEvent(@Nonnull User contact, @Nonnull ContactUiEventType type) {
		super(contact, type, null);
	}

	@Nonnull
	public User getContact() {
		return getEventObject();
	}
}
