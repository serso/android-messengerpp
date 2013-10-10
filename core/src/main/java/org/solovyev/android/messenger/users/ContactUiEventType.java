package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactUiEventType {

	contact_clicked,
	call_contact,
	edit_contact,
	mark_all_messages_read,
	open_contact_chat,
	show_composite_user_dialog
	;

	@Nonnull
	public ContactUiEvent newEvent(@Nonnull User contact) {
		return new ContactUiEvent(contact, this);
	}
}
