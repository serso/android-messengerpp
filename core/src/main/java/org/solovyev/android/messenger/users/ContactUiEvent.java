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

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.common.listeners.JEvent;

import javax.annotation.Nonnull;

public abstract class ContactUiEvent implements JEvent {

	@Nonnull
	public final User contact;

	protected ContactUiEvent(@Nonnull User contact) {
		this.contact = contact;
	}

	public static class Clicked extends ContactUiEvent {

		public Clicked(@Nonnull User contact) {
			super(contact);
		}
	}

	public static class OpenChat extends ContactUiEvent {

		@Nonnull
		public final Account account;

		public OpenChat(@Nonnull User contact, @Nonnull Account account) {
			super(contact);
			this.account = account;
		}
	}

	public static class Edit extends ContactUiEvent {

		@Nonnull
		public final Account account;

		public Edit(@Nonnull User contact, @Nonnull Account account) {
			super(contact);
			this.account = account;
		}
	}

	public static class ShowCompositeDialog extends ContactUiEvent {

		@Nonnull
		public final ContactUiEventType nextEventType;

		public ShowCompositeDialog(@Nonnull User contact, @Nonnull ContactUiEventType nextEventType) {
			super(contact);
			this.nextEventType = nextEventType;
		}
	}

	public static class Typed extends ContactUiEvent {

		@Nonnull
		public final ContactUiEventType type;

		public Typed(@Nonnull User contact, @Nonnull ContactUiEventType type) {
			super(contact);
			this.type = type;
		}
	}

	public static class Saved extends ContactUiEvent {
		public Saved(@Nonnull User contact) {
			super(contact);
		}
	}
}
