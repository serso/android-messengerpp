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

package org.solovyev.android.messenger.messages;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

public interface MutableMessage extends Message {

	void setOriginalId(@Nonnull String id);

	void setAuthor(@Nonnull Entity author);

	void setSendDate(@Nonnull DateTime sendDate);

	void setTitle(@Nonnull String title);

	void setBody(@Nonnull String body);

	void setRecipient(@Nullable Entity recipient);

	void setState(@Nonnull MessageState state);

	void setChat(@Nonnull Entity chat);

	void setRead(boolean read);

	@Nonnull
	@Override
	MutableMessage clone();

	@Nonnull
	MutableMessage cloneRead();

	@Nonnull
	@Override
	MutableMessage cloneWithNewState(@Nonnull MessageState state);

	@Nonnull
	@Override
	MutableAProperties getProperties();

	void setProperties(@Nonnull List<AProperty> properties);

	@Nonnull
	MutableMessage cloneWithNewChat(@Nonnull Entity chat);

	@Nonnull
	@Override
	MutableMessage merge(@Nonnull Message that);
}
