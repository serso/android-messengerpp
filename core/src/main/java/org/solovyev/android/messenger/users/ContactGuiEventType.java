package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactGuiEventType {

	contact_clicked;

	@Nonnull
	public static ContactGuiEvent newContactClicked(@Nonnull User contact) {
		return new ContactGuiEvent(contact, contact_clicked);
	}
}
