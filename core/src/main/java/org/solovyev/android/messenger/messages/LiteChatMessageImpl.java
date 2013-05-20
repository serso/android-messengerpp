package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerEntity;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:04 PM
 */
public final class LiteChatMessageImpl extends AbstractMessengerEntity implements LiteChatMessage {

	@Nonnull
	private Entity author;

	@Nullable
	private Entity recipient;

	@Nonnull
	private DateTime sendDate;

	@Nonnull
	private String title = "";

	@Nonnull
	private String body = "";

	private LiteChatMessageImpl(@Nonnull Entity entity) {
		super(entity);
	}

	@Nonnull
	static LiteChatMessageImpl newInstance(@Nonnull Entity entity) {
		return new LiteChatMessageImpl(entity);
	}

	@Nonnull
	public Entity getAuthor() {
		return author;
	}

	public void setAuthor(@Nonnull Entity author) {
		this.author = author;
	}

	@Nonnull
	public DateTime getSendDate() {
		return sendDate;
	}

	public void setSendDate(@Nonnull DateTime sendDate) {
		this.sendDate = sendDate;
	}

	@Nonnull
	public String getTitle() {
		return title;
	}

	public void setTitle(@Nonnull String title) {
		this.title = title;
	}

	@Nonnull
	public String getBody() {
		return body;
	}

	@Nonnull
	@Override
	public LiteChatMessageImpl clone() {
		final LiteChatMessageImpl clone = (LiteChatMessageImpl) super.clone();

		clone.author = this.author.clone();

		if (this.recipient != null) {
			clone.recipient = this.recipient.clone();
		}

		return clone;
	}

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

	public void setRecipient(@Nullable Entity recipient) {
		this.recipient = recipient;
	}
}
