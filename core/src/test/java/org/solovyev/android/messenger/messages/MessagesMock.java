package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.messages.MessageState.created;
import static org.solovyev.android.properties.Properties.newProperties;

public class MessagesMock {

	@Nonnull
	private static final AtomicInteger counter = new AtomicInteger();

	@Nonnull
	public static Message newMockMessage() {
		return newMockMessage(DateTime.now());
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate) {
		final Entity from = mock(Entity.class);
		final Entity to = mock(Entity.class);

		return newMockMessage(sendDate, from, to, "test");
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate,
											 @Nonnull Entity from,
											 @Nonnull Entity to,
											 @Nonnull Account account) {
		return newMockMessage(sendDate, from, to, account.getId());
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate,
											 @Nonnull Entity from,
											 @Nonnull Entity to,
											 @Nonnull String accountId) {
		final Message message = mock(Message.class);

		final String id = getMessageId();
		when(message.getEntity()).thenReturn(newEntity(accountId, id));
		when(message.getId()).thenReturn(String.valueOf(id));
		when(message.getBody()).thenReturn("body_" + id);
		when(message.getTitle()).thenReturn("title_" + id);

		when(message.getAuthor()).thenReturn(from);

		when(message.getRecipient()).thenReturn(to);
		when(message.getState()).thenReturn(created);

		when(message.getSendDate()).thenReturn(sendDate);
		final MutableAProperties properties = newProperties(Collections.<AProperty>emptyList());
		properties.setProperty("property_1", "test");
		properties.setProperty("property_2", "42");
		when(message.getProperties()).thenReturn(properties);
		return message;
	}

	static String getMessageId() {
		return String.valueOf(counter.getAndIncrement());
	}
}
