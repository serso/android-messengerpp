package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactGuiEventType {

	contact_clicked,
	open_contact_chat,
	show_composite_user_dialog
	;

	@Nonnull
	public static ContactGuiEvent newContactClicked(@Nonnull User contact) {
		return new ContactGuiEvent(contact, contact_clicked);
	}

	@Nonnull
	public static ContactGuiEvent newOpenContactChat(@Nonnull User contact) {
		return new ContactGuiEvent(contact, open_contact_chat);
	}

	@Nonnull
	public static ContactGuiEvent newShowCompositeUserDialog(@Nonnull User contact) {
		return new ContactGuiEvent(contact, show_composite_user_dialog);
	}
}
