package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 1:07 AM
 */
public enum ContactUiEventType {

	contact_clicked,
	call_contact,
	resend_message,
	edit_contact,
	mark_all_messages_read,
	open_contact_chat,
	show_composite_user_dialog {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof ContactUiEventType;
		}
	}
	;

	@Nonnull
	public ContactUiEvent newEvent(@Nonnull User contact) {
		return newEvent(contact, null);
	}

	@Nonnull
	public ContactUiEvent newEvent(@Nonnull User contact, @Nullable Object data) {
		checkData(data);
		return new ContactUiEvent(contact, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}
}
