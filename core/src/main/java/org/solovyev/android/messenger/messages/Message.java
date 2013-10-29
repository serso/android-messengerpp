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
