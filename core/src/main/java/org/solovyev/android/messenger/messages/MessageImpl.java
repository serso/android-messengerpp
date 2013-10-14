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

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:04 PM
 */
final class MessageImpl extends AbstractIdentifiable implements MutableMessage {

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
		final MessageImpl clone = clone();
		clone.state = state;
		return clone;
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
}
