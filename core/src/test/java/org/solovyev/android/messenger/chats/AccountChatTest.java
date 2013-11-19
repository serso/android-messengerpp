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
		final MutableChat chat = Chats.newEmptyChat("test:test");
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
