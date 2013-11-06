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

package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.Mergeable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface Chat extends Identifiable, EntityAware, Mergeable<Chat> {

	String PROPERTY_PRIVATE = "private";
	String PROPERTY_DRAFT_MESSAGE = "draft_message";

	@Nonnull
	Entity getEntity();

	boolean isPrivate();

	// must be called only after isPrivate() check
	@Nonnull
	Entity getSecondUser();

	@Nullable
	DateTime getLastMessagesSyncDate();

	@Nonnull
	Collection<AProperty> getPropertiesCollection();

	@Nonnull
	Chat updateMessagesSyncDate();

	/**
	 * Method creates copy of this object with new account id
	 *
	 * @param id new chat id
	 * @return chat copy with updated properties
	 */
	@Nonnull
	Chat copyWithNewId(@Nonnull Entity id);

	@Nonnull
	Chat cloneWithNewProperty(@Nonnull AProperty property);

	@Nullable
	String getPropertyValueByName(@Nonnull String name);
}
