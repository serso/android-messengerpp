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

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.TestAccount;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserSameEqualizer;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.MutableObject;
import org.solovyev.common.listeners.AbstractJEventListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterables.any;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.solovyev.android.messenger.chats.Chats.newPrivateAccountChat;
import static org.solovyev.android.messenger.messages.Messages.newIncomingMessage;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.common.Objects.areEqual;

public class ChatServiceTest extends DefaultMessengerTest {

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private UserService userService;

	@Test
	public void testMessagesShouldBeReturnedInDescendingOrder() throws Exception {
		final Account account = getAccount2();
		final User user = account.getUser();

		createChats(account, user, true);

		final List<UiChat> chats = chatService.getLastUiChats(user, null, MAX_VALUE);
		assertTrue(!chats.isEmpty());
		for (int i = 1; i < chats.size(); i++) {
			final Message prevMessage = chats.get(i - 1).getLastMessage();
			final Message message = chats.get(i).getLastMessage();
			assertNotNull(prevMessage);
			assertNotNull(message);
			assertTrue(prevMessage.getSendDate().isAfter(message.getSendDate()));
		}
	}

	private void createChats(@Nonnull Account account, @Nonnull User user, boolean createMessages) throws AccountException {
		final DateTime now = DateTime.now();

		final List<User> contacts = userService.getUserContacts(user.getEntity());
		assert !contacts.isEmpty();

		for (int i = 0; i < contacts.size(); i++) {
			final User contact = contacts.get(i);
			final Chat chat = chatService.getOrCreatePrivateChat(user.getEntity(), contact.getEntity());

			if (createMessages) {
				final List<Message> messages = new ArrayList<Message>();
				for (int j = 0; j < 10; j++) {
					messages.add(newMockMessage(now.plusHours(i + 5 * j), user.getEntity(), contact.getEntity(), account, chat.getId()));
				}
				chatService.saveMessages(chat.getEntity(), messages);
			}
		}
	}

	@Test
	public void testEmptyChatsShouldNotBeInList() throws Exception {
		final Account account = getAccount2();
		final User user = account.getUser();

		createChats(account, user, true);
		createChats(account, user, false);

		final List<UiChat> chats = chatService.getLastUiChats(user, null, MAX_VALUE);
		assertTrue(!chats.isEmpty());
		for (UiChat chat : chats) {
			assertNotNull(chat.getLastMessage());
		}
	}

	@Test
	public void testChatIdShouldBeSetOnSaveForNewPrivateChat() throws Exception {
		final AccountData ad = getAccountData1();
		final TestAccount account = ad.getAccount();
		final User user = account.getUser();
		final User contact = ad.getContacts().get(0);

		final Chat chat = chatService.saveChat(user.getEntity(), newPrivateAccountChat(account.newChatEntity("test_api_chat"), user, contact, Collections.<MutableMessage>emptyList()));

		assertNotNull(chat);
		assertEquals(chatService.getPrivateChatId(user.getEntity(), contact.getEntity()), chat.getEntity());
		assertEquals(chatService.getPrivateChatId(user.getEntity(), contact.getEntity()).getAccountEntityId(), chat.getEntity().getAccountEntityId());
	}

	@Test
	public void testSavedChatShouldNotBeNullForExistingChat() throws Exception {
		final AccountData ad = getAccountData1();
		final TestAccount account = ad.getAccount();
		final User user = account.getUser();
		final AccountChat accountChat = ad.getChats().get(0);

		final Chat chat = chatService.saveChat(user.getEntity(), accountChat);

		assertTrue(areEqual(chat, accountChat.getChat(), new ChatSameEqualizer()));
	}

	@Test
	public void testShouldSaveParticipantIfDoesntExist() throws Exception {
		final AccountData ad = getAccountData1();
		final User user = ad.getAccount().getUser();
		final User contact = newEmptyUser(ad.getAccount().newEntity("test_sdfsde5t"));
		final Entity chat = ad.getAccount().newChatEntity("test_23123123");
		final MutableAccountChat accountChat = newPrivateAccountChat(chat, user, contact, Collections.<MutableMessage>emptyList());

		try {
			userService.getUserById(contact.getEntity(), false, false);
			fail();
		} catch (NoSuchElementException e) {
			// ok, user doesn't exist
		}

		chatService.mergeUserChats(user.getEntity(), asList(accountChat));

		final User actual = userService.getUserById(contact.getEntity());
		assertTrue(areEqual(contact, actual, new UserSameEqualizer()));
	}

	@Test
	public void testOnUserRemovalChatShouldBeRemoved() throws Exception {
		final AccountData ad = getAccountData1();
		final User contact = ad.getContacts().get(0);

		userService.removeUser(contact);

		final List<Chat> chats = userService.getUserChats(ad.getAccount().getUser().getEntity());
		assertFalse(any(chats, new Predicate<Chat>() {
			@Override
			public boolean apply(Chat chat) {
				return chat.isPrivate() && chat.getSecondUser().getEntityId().equals(contact.getId());
			}
		}));
	}

	@Test
	public void testShouldNotifyAboutNewMessages() throws Exception {
		final AccountData ad = getAccountData1();
		final User user = ad.getAccount().getUser();
		final User contact = newEmptyUser(ad.getAccount().newEntity("test_sdfsde5t"));
		final Entity chat = ad.getAccount().newChatEntity("test_23123123");
		final MutableAccountChat accountChat = newPrivateAccountChat(chat, user, contact, Collections.<MutableMessage>emptyList());
		accountChat.addMessage(newIncomingMessage(ad.getAccount(), accountChat.getChat(), "test1", "", user.getEntity()));
		accountChat.addMessage(newIncomingMessage(ad.getAccount(), accountChat.getChat(), "test2", "", user.getEntity()));

		final MutableObject<List<Message>> addedMessages = new MutableObject<List<Message>>();
		chatService.addListener(new AbstractJEventListener<ChatEvent>(ChatEvent.class) {
			@Override
			public void onEvent(@Nonnull ChatEvent event) {
				switch (event.getType()) {
					case messages_added:
						addedMessages.setObject(event.getDataAsMessages());
						break;
				}
			}
		});

		chatService.mergeUserChats(user.getEntity(), asList(accountChat));

		assertEquals(2, addedMessages.getObject().size());
		assertEquals("test1", addedMessages.getObject().get(0).getBody());
		assertEquals("test2", addedMessages.getObject().get(1).getBody());
	}
}
