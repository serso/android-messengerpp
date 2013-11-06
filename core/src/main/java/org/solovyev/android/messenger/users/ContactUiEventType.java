/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	view_contact,
	mark_all_messages_read,
	open_contact_chat,
	show_composite_user_dialog {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof ContactUiEventType;
		}
	};

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
