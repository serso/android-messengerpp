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

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
