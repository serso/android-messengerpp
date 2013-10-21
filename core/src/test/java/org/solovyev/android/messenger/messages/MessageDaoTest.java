package org.solovyev.android.messenger.messages;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.PropertiesEqualizer;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.Equalizer;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

public class MessageDaoTest extends DefaultDaoTest<Message> {

	@Inject
	@Nonnull
	private SqliteMessageDao dao;

	@Inject
	@Nonnull
	private SqliteChatDao chatDao;

	@Inject
	@Nonnull
	private ChatService chatService;

	public MessageDaoTest() {
		super(null, new MessageSameEqualizer());
	}

	@Test
	public void testLastMessageShouldBeMessageWithLastDate() throws Exception {
		final Account account = getAccount1();
		final org.solovyev.android.messenger.entities.Entity from = account.getUser().getEntity();
		final org.solovyev.android.messenger.entities.Entity to = getContactForAccount(account, 0).getEntity();

		final Chat chat = chatService.getOrCreatePrivateChat(from, to);
		final List<Message> messages = new ArrayList<Message>();
		final DateTime now = DateTime.now();
		messages.add(newMockMessage(now, from, to, account, chat.getId()));
		messages.add(newMockMessage(now.plusDays(1), from, to, account, chat.getId()));
		messages.add(newMockMessage(now.plusDays(2), from, to, account, chat.getId()));
		messages.add(newMockMessage(now.plusDays(3), from, to, account, chat.getId()));
		dao.mergeMessages(chat.getId(), messages);

		checkLastMessage(chat, now.plusDays(3));

		dao.mergeMessages(chat.getId(), Arrays.asList(newMockMessage(now.plusDays(4), from, to, account, chat.getId())));
		checkLastMessage(chat, now.plusDays(4));
	}

	@Test
	public void testShouldSaveProperties() throws Exception {
		final AccountData ad = getAccountData1();
		final AccountChat chat = ad.getChats().get(0);

		final MutableMessage expected = newMessageWithProperties(ad);
		dao.mergeMessages(chat.getChat().getId(), Arrays.asList(expected));

		final Message actual = dao.read(expected.getId());
		assertEquals("test", actual.getProperties().getPropertyValue("property_1"));
		assertEquals("42", actual.getProperties().getPropertyValue("property_2"));
	}


	@Test
	public void testShouldUpdateProperties() throws Exception {
		final AccountData ad = getAccountData1();
		final AccountChat chat = ad.getChats().get(0);

		final MutableMessage expected = newMessageWithProperties(ad);
		dao.mergeMessages(chat.getChat().getId(), Arrays.asList(expected));

		expected.getProperties().setProperty("property_1", "test2");
		expected.getProperties().removeProperty("property_2");
		dao.mergeMessages(chat.getChat().getId(), Arrays.asList(expected));


		final Message actual = dao.read(expected.getId());
		assertEquals("test2", actual.getProperties().getPropertyValue("property_1"));
		assertNull(actual.getProperties().getPropertyValue("property_2"));
	}

	private MutableMessage newMessageWithProperties(AccountData ad) {
		final MutableMessage expected = Messages.newMessage(ad.getAccount().newMessageEntity(MessagesMock.getMessageId()));
		expected.getProperties().setProperty("property_1", "test");
		expected.getProperties().setProperty("property_2", "42");
		expected.setAuthor(ad.getAccount().getUser().getEntity());
		expected.setRecipient(ad.getContacts().get(0).getEntity());
		expected.setSendDate(DateTime.now());
		expected.setBody("test");
		expected.setChat(ad.getChats().get(0).getChat().getEntity());
		return expected;
	}

	private void checkLastMessage(@Nonnull Chat chat, @Nonnull DateTime expected) {
		Message lastMessage = dao.readLastMessage(chat.getId());
		assertNotNull(lastMessage);
		assertEquals(lastMessage.getSendDate(), expected);
	}

	@Nonnull
	@Override
	protected Dao<Message> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected String getId(Message entity) {
		return entity.getId();
	}

	@Nonnull
	@Override
	protected Collection<Message> populateEntities(@Nonnull Dao<Message> dao) {
		final ArrayList<Message> messages = new ArrayList<Message>();
		for (AccountData accountData : getAccountDataList()) {
			for (AccountChat accountChat : accountData.getChats()) {
				messages.addAll(accountChat.getMessages());
			}
		}
		return messages;
	}

	@Nonnull
	@Override
	protected Entity<Message> newInsertEntity() {
		final Message message = newMessageWithProperties(getAccountData1());
		return newEntity(message, message.getId());
	}

	@Nonnull
	@Override
	protected Message changeEntity(@Nonnull Message message) {
		return message;
	}

	private static class MessageSameEqualizer implements Equalizer<Message> {
		@Override
		public boolean areEqual(@Nonnull Message m1, @Nonnull Message m2) {
			boolean same = Objects.areEqual(m1.getEntity(), m2.getEntity());

			same &= Objects.areEqual(m1.isRead(), m2.isRead());
			same &= Objects.areEqual(m1.isPrivate(), m2.isPrivate());
			same &= Objects.areEqual(m1.getBody(), m2.getBody());
			same &= Objects.areEqual(m1.getTitle(), m2.getTitle());
			same &= Objects.areEqual(m1.getSendDate(), m2.getSendDate());
			same &= Objects.areEqual(m1.getProperties(), m2.getProperties(), new PropertiesEqualizer());

			return same;
		}
	}
}
