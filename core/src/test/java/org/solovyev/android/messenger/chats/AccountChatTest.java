package org.solovyev.android.messenger.chats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import static org.junit.Assert.assertEquals;

public class AccountChatTest {

	@Test
	public void testShouldUpdateChatInMessagesOnIdUpdate() throws Exception {
		final Chat chat = Chats.newEmptyChat("test:test");
		final List<Message> messages = new ArrayList<Message>();
		for (int i = 0; i < 10; i++) {
			final MutableMessage message = Messages.newEmptyMessage("test:test" + i);
			message.setAuthor(Entities.newEntity("test", "test"));
			message.setChat(chat.getEntity());
			messages.add(message);

		}
		final AccountChat accountChat = new AccountChatImpl(chat, messages, Collections.<User>emptyList());

		final Entity newId = Entities.newEntity("456", "123");

		final AccountChat actual = accountChat.copyWithNewId(newId);

		assertEquals(newId, actual.getChat().getEntity());
		assertEquals(10, actual.getMessages().size());
		for (Message message : actual.getMessages()) {
			assertEquals(newId, message.getChat());
		}
	}
}
