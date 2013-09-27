package org.solovyev.android.messenger.messages;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessagesMock {

	@Nonnull
	private static final AtomicInteger counter = new AtomicInteger();

	@Nonnull
	public static ChatMessage newMockMessage(@Nonnull DateTime sendDate) {
		final ChatMessage message = mock(ChatMessage.class);
		final int id = counter.getAndIncrement();
		final Entity entity = mock(Entity.class);
		when(entity.getEntityId()).thenReturn(String.valueOf(id));
		when(message.getEntity()).thenReturn(entity);
		when(message.getId()).thenReturn(String.valueOf(id));
		when(message.getBody()).thenReturn("body_" + id);

		final Entity author = mock(Entity.class);
		when(message.getAuthor()).thenReturn(author);

		final Entity recipient = mock(Entity.class);
		when(message.getRecipient()).thenReturn(recipient);

		when(message.getSendDate()).thenReturn(sendDate);
		return message;
	}
}
