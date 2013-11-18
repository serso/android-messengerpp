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
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.solovyev.android.messenger.AbstractIdentifiable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.solovyev.android.properties.Properties.newProperties;
import static org.solovyev.common.text.Strings.isEmpty;

final class MessageImpl extends AbstractIdentifiable implements MutableMessage {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	private static final String PROPERTY_ORIGINAL_ID = "original_id";

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private Entity author;

	@Nullable
	private Entity recipient;

	@Nonnull
	private DateTime sendDate;

	@Nullable
	private DateTime localSendDateTime;

	@Nullable
	private LocalDate localSendDate;

	@Nonnull
	private String title = "";

	@Nonnull
	private String body = "";

	@Nonnull
	private MessageState state = MessageState.created;

	@Nonnull
	private Entity chat;

	private boolean read = false;

	@Nonnull
	private MutableAProperties properties = newProperties(Collections.<AProperty>emptyList());

	MessageImpl(@Nonnull Entity entity) {
		super(entity);
	}

	@Nonnull
	@Override
	public String getOriginalId() {
		final String originalId = properties.getPropertyValue(PROPERTY_ORIGINAL_ID);
		return !isEmpty(originalId) ? originalId : getId();
	}

	@Override
	public void setOriginalId(@Nonnull String id) {
		properties.setProperty(PROPERTY_ORIGINAL_ID, id);
	}

	@Nonnull
	public Entity getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(@Nonnull Entity author) {
		this.author = author;
	}

	@Nonnull
	public DateTime getSendDate() {
		return sendDate;
	}

	@Override
	public void setSendDate(@Nonnull DateTime sendDate) {
		this.sendDate = sendDate;
		this.localSendDateTime = null;
		this.localSendDate = null;
	}

	@Nonnull
	@Override
	public DateTime getLocalSendDateTime() {
		if(localSendDateTime == null) {
			final DateTimeZone localTimeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
			localSendDateTime = sendDate.toDateTime(localTimeZone);
		}
		return localSendDateTime;
	}

	@Nonnull
	@Override
	public LocalDate getLocalSendDate() {
		if (localSendDate == null) {
			localSendDate = getLocalSendDateTime().toLocalDate();
		}
		return localSendDate;
	}

	@Nonnull
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(@Nonnull String title) {
		this.title = title;
	}

	@Nonnull
	public String getBody() {
		return body;
	}

	@Nonnull
	@Override
	public MessageState getState() {
		return state;
	}

	@Nonnull
	@Override
	public MessageImpl clone() {
		final MessageImpl clone = (MessageImpl) super.clone();

		clone.author = this.author.clone();
		clone.chat = this.chat.clone();
		clone.properties = this.properties.clone();

		if (this.recipient != null) {
			clone.recipient = this.recipient.clone();
		}

		return clone;
	}

	@Nonnull
	@Override
	public MutableMessage cloneWithNewState(@Nonnull MessageState state) {
		if (this.state != state) {
			final MessageImpl clone = clone();
			clone.state = state;
			return clone;
		} else {
			return this;
		}
	}

	@Nonnull
	@Override
	public MutableAProperties getProperties() {
		return properties;
	}

	@Override
	public void setProperties(@Nonnull List<AProperty> properties) {
		this.properties.setPropertiesFrom(properties);
	}

	@Nonnull
	@Override
	public MutableMessage cloneWithNewChat(@Nonnull Entity chat) {
		if (!this.chat.equals(chat)) {
			final MessageImpl clone = clone();
			clone.chat = chat;
			return clone;
		} else {
			return this;
		}
	}

	@Nonnull
	@Override
	public MutableMessage merge(@Nonnull Message that) {
		if(this == that) {
			return this;
		} else {
			final MessageImpl clone = clone();
			// NOTE: author, recipient, id, chat cannot be changed => do not apply them from that instance
			if (!clone.read) {
				clone.read = that.isRead();
			}

			if(clone.state != MessageState.removed) {
				clone.state = that.getState();
			}

			clone.body = that.getBody();
			if (!isEmpty(that.getTitle())) {
				clone.title = that.getTitle();
			}
			clone.sendDate = that.getSendDate();
			// NOTE: we don't remove properties here as some properties are application related, e.g. PROPERTY_ORIGINAL_ID
			clone.properties.setPropertiesFrom(that.getProperties().getPropertiesCollection());
			return clone;
		}
	}

	@Override
	public boolean isOutgoing() {
		return state.isOutgoing();
	}

	@Override
	public boolean isIncoming() {
		return state.isIncoming();
	}

	@Override
	public void setBody(@Nonnull String body) {
		this.body = body;
	}

	@Nullable
	public Entity getRecipient() {
		return recipient;
	}

	@Override
	public boolean isPrivate() {
		return recipient != null && !recipient.equals(author);
	}

	@Override
	public Entity getSecondUser(@Nonnull Entity user) {
		if (user.equals(author)) {
			return recipient;
		} else if (user.equals(recipient)) {
			return author;
		}

		return null;
	}

	@Override
	public void setRecipient(@Nullable Entity recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setState(@Nonnull MessageState state) {
		this.state = state;
	}

	@Override
	@Nonnull
	public Entity getChat() {
		return chat;
	}

	@Override
	public void setChat(@Nonnull Entity chat) {
		this.chat = chat;
	}

	@Override
	public boolean isRead() {
		return read;
	}

	@Override
	public boolean canRead() {
		return isIncoming() && !isRead();
	}

	@Override
	public void setRead(boolean read) {
		this.read = read;
	}

	@Nonnull
	@Override
	public MutableMessage cloneRead() {
		final MessageImpl clone = clone();
		clone.read = true;
		return clone;
	}

	@Override
	public String toString() {
		return "Message{" +
				"id=" + getEntity() +
				", chat=" + chat +
				", body='" + body + '\'' +
				", recipient=" + recipient +
				", author=" + author +
				", sendDate=" + sendDate +
				'}';
	}
}
