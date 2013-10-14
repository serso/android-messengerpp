package org.solovyev.android.messenger.messages;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

public class MessageDaoTest extends DefaultMessengerTest {

	@Inject
	@Nonnull
	private SqliteMessageDao dao;

	@Inject
	@Nonnull
	private SqliteChatDao chatDao;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Test
	public void testLastMessageShouldBeMessageWithLastDate() throws Exception {
		final Account account = getAccount1();
		final Entity from = account.getUser().getEntity();
		final Entity to = getContactForAccount(account, 0).getEntity();

		final List<Message> messages = new ArrayList<Message>();
		final DateTime now = DateTime.now();
		messages.add(newMockMessage(now, from, to, account));
		messages.add(newMockMessage(now.plusDays(1), from, to, account));
		messages.add(newMockMessage(now.plusDays(2), from, to, account));
		messages.add(newMockMessage(now.plusDays(3), from, to, account));
		final Chat chat = chatService.getOrCreatePrivateChat(from, to);
		dao.mergeMessages(chat.getId(), messages, false);

		checkLastMessage(chat, now.plusDays(3));

		dao.mergeMessages(chat.getId(), Arrays.asList(newMockMessage(now.plusDays(4), from, to, account)), false);
		checkLastMessage(chat, now.plusDays(4));

	}

	private void checkLastMessage(@Nonnull Chat chat, @Nonnull DateTime expected) {
		Message lastMessage = dao.readLastMessage(chat.getId());
		assertNotNull(lastMessage);
		assertEquals(lastMessage.getSendDate(), expected);
	}
}
