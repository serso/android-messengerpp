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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.Mergeable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Message extends Identifiable, Mergeable<Message> {

	@Nonnull
	Entity getEntity();

	/**
	 * @return id of a message which has been assigned before sending message
	 */
	@Nonnull
	String getOriginalId();

	@Nonnull
	Entity getAuthor();

	@Nullable
	Entity getRecipient();

	boolean isPrivate();

	@Nullable
	Entity getSecondUser(@Nonnull Entity user);

	@Nonnull
	DateTime getSendDate();

	@Nonnull
	DateTime getLocalSendDateTime();

	@Nonnull
	LocalDate getLocalSendDate();

	@Nonnull
	String getTitle();

	@Nonnull
	String getBody();

	@Nonnull
	MessageState getState();

	@Nonnull
	Message clone();

	@Nonnull
	Message cloneRead();

	@Nonnull
	Message cloneWithNewState(@Nonnull MessageState state);

	@Nonnull
	Entity getChat();

	boolean isRead();

	boolean canRead();

	@Nonnull
	AProperties getProperties();

	@Nonnull
	Message cloneWithNewChat(@Nonnull Entity chat);

	boolean isOutgoing();

	boolean isIncoming();
}
