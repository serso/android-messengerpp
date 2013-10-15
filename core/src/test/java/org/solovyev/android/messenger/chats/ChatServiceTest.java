package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.TestAccount;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.Assert.*;
import static org.solovyev.android.messenger.chats.Chats.newPrivateAccountChat;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

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

		final List<UiChat> chats = chatService.getLastChats(user, MAX_VALUE);
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
					messages.add(newMockMessage(now.plusHours(i + 5 * j), user.getEntity(), contact.getEntity(), account));
				}
				chatService.saveMessages(chat.getEntity(), messages, false);
			}
		}
	}

	@Test
	public void testEmptyChatsShouldNotBeInList() throws Exception {
		final Account account = getAccount2();
		final User user = account.getUser();

		createChats(account, user, true);
		createChats(account, user, false);

		final List<UiChat> chats = chatService.getLastChats(user, MAX_VALUE);
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

		final List<User> participants = Arrays.asList(user, contact);
		final Chat chat = chatService.saveChat(user.getEntity(), newPrivateAccountChat(account.newChatEntity("test_api_chat"), participants, Collections.<Message>emptyList()));

		assertNotNull(chat);
		assertEquals(chatService.getPrivateChatId(user.getEntity(), contact.getEntity()), chat.getEntity());
		assertEquals("test_api_chat", chat.getEntity().getAccountEntityId());
	}

	@Test
	public void testSavedChatShouldNotBeNullForExistingChat() throws Exception {
		final AccountData ad = getAccountData1();
		final TestAccount account = ad.getAccount();
		final User user = account.getUser();
		final AccountChat accountChat = ad.getChats().get(0);

		final Chat chat = chatService.saveChat(user.getEntity(), accountChat);

		assertTrue(Objects.areEqual(chat, accountChat.getChat(), new ChatSameEqualizer()));
	}
}
