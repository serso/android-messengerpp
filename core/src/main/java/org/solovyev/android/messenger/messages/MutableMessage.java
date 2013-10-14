package org.solovyev.android.messenger.messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;

public interface MutableMessage extends Message {

	void setAuthor(@Nonnull Entity author);

	void setSendDate(@Nonnull DateTime sendDate);

	void setTitle(@Nonnull String title);

	void setBody(@Nonnull String body);

	void setRecipient(@Nullable Entity recipient);

	void setState(@Nonnull MessageState state);

	void setChat(@Nonnull Entity chat);
}
