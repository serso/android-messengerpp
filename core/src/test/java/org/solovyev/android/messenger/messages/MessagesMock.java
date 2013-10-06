package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.entities.Entities.newEntity;

public class MessagesMock {

	@Nonnull
	private static final AtomicInteger counter = new AtomicInteger();

	@Nonnull
	public static ChatMessage newMockMessage(@Nonnull DateTime sendDate) {
		final Entity from = mock(Entity.class);
		final Entity to = mock(Entity.class);

		return newMockMessage(sendDate, from, to, "test");
	}

	@Nonnull
	public static ChatMessage newMockMessage(@Nonnull DateTime sendDate,
											 @Nonnull Entity from,
											 @Nonnull Entity to,
											 @Nonnull Account account) {
		return newMockMessage(sendDate, from, to, account.getId());
	}

	@Nonnull
	public static ChatMessage newMockMessage(@Nonnull DateTime sendDate,
											 @Nonnull Entity from,
											 @Nonnull Entity to,
											 @Nonnull String accountId) {
		final ChatMessage message = mock(ChatMessage.class);

		final String id = String.valueOf(counter.getAndIncrement());
		when(message.getEntity()).thenReturn(newEntity(accountId, id));
		when(message.getId()).thenReturn(String.valueOf(id));
		when(message.getBody()).thenReturn("body_" + id);
		when(message.getTitle()).thenReturn("title_" + id);

		when(message.getAuthor()).thenReturn(from);

		when(message.getRecipient()).thenReturn(to);

		when(message.getSendDate()).thenReturn(sendDate);
		return message;
	}
}
