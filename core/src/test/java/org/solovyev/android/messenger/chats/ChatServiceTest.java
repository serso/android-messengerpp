package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTestCase;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

public class ChatServiceTest extends DefaultMessengerTestCase {

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
			final ChatMessage prevMessage = chats.get(i - 1).getLastMessage();
			final ChatMessage message = chats.get(i).getLastMessage();
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
				final List<ChatMessage> messages = new ArrayList<ChatMessage>();
				for (int j = 0; j < 10; j++) {
					messages.add(newMockMessage(now.plusHours(i + 5 * j), user.getEntity(), contact.getEntity(), account));
				}
				chatService.saveChatMessages(chat.getEntity(), messages, false);
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
}
