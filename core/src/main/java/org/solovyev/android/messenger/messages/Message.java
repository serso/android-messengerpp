package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:58 PM
 */
public interface Message extends Identifiable {

	@Nonnull
	Entity getEntity();

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

	@Nonnull
	AProperties getProperties();

	@Nonnull
	Message cloneWithNewChat(@Nonnull Entity chat);
}
