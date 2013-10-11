package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public class ContactUiEvent extends AbstractTypedJEvent<User, ContactUiEventType> {

	public ContactUiEvent(@Nonnull User contact, @Nonnull ContactUiEventType type, @Nullable Object data) {
		super(contact, type, data);
	}

	@Nonnull
	public User getContact() {
		return getEventObject();
	}

	@Nonnull
	public ContactUiEventType getDataAsEventType() {
		return (ContactUiEventType) getData();
	}
}
