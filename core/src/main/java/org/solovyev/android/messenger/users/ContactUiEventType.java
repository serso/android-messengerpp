package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactUiEventType {

	contact_clicked,
	open_contact_chat,
	show_composite_user_dialog
	;

	@Nonnull
	public static ContactUiEvent newContactClicked(@Nonnull User contact) {
		return new ContactUiEvent(contact, contact_clicked);
	}

	@Nonnull
	public static ContactUiEvent newOpenContactChat(@Nonnull User contact) {
		return new ContactUiEvent(contact, open_contact_chat);
	}

	@Nonnull
	public static ContactUiEvent newShowCompositeUserDialog(@Nonnull User contact) {
		return new ContactUiEvent(contact, show_composite_user_dialog);
	}
}
