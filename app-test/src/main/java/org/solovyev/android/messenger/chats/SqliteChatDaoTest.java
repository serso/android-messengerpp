package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.*;
import org.solovyev.android.messenger.realms.TestAccount;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqliteChatDaoTest extends AbstractMessengerTestCase {

	@Inject
	private UserDao userDao;

	@Inject
	private ChatDao chatDao;

	@Inject
	private ChatMessageDao chatMessageDao;

	@Inject
	private TestRealm testRealmDef;

	@Inject
	private TestAccount testRealm;

	public void setUp() throws Exception {
		super.setUp();
		chatMessageDao.deleteAll();
		chatDao.deleteAll();
	}

	public void testChatOperations() throws Exception {

		final ArrayList<ApiChat> chats = new ArrayList<ApiChat>();

		final Entity realmUser = testRealm.newUserEntity("01");
		final String userId = realmUser.getEntityId();

		userDao.create(Users.newEmptyUser(realmUser));

		chatDao.mergeChats(userId, chats);

		Assert.assertTrue(chatDao.readChatsByUserId(userId).isEmpty());

		final Entity realmChat1 = testRealm.newChatEntity("01");
		chats.add(ApiChatImpl.newInstance(realmChat1, 10, false));
		final Entity realmChat2 = testRealm.newChatEntity("02");
		chats.add(ApiChatImpl.newInstance(realmChat2, 10, false));
		final Entity realmChat3 = testRealm.newChatEntity("03");
		chats.add(ApiChatImpl.newInstance(realmChat3, 10, false));
		final Entity realmChat4 = testRealm.newChatEntity("04");
		chats.add(ApiChatImpl.newInstance(realmChat4, 10, false));
		chatDao.mergeChats(userId, chats);

		Chat chat = chatDao.read(realmChat4.getEntityId());
		Assert.assertNotNull(chat);
		Assert.assertEquals(realmChat4.getEntityId(), chat.getEntity().getEntityId());

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		messages.add(newMessage("01", false));
		messages.add(newMessage("02", false));
		messages.add(newMessage("03", true));
		messages.add(newMessage("04", true));
		chatMessageDao.mergeMessages(realmChat4.getEntityId(), messages, false);

		messages = new ArrayList<ChatMessage>();
		messages.add(newMessage("07", true));
		messages.add(newMessage("08", false));
		messages.add(newMessage("09", true));
		messages.add(newMessage("06", true));
		chatMessageDao.mergeMessages(realmChat1.getEntityId(), messages, false);

		messages = new ArrayList<ChatMessage>();
		messages.add(newMessage("10", true));
		messages.add(newMessage("11", true));
		chatMessageDao.mergeMessages(realmChat2.getEntityId(), messages, false);

		final Map<Entity, Integer> actualUnreadChats = chatDao.getUnreadChats();
		assertFalse(actualUnreadChats.isEmpty());
		assertTrue(actualUnreadChats.containsKey(realmChat1));
		assertTrue(actualUnreadChats.containsKey(realmChat4));
		assertFalse(actualUnreadChats.containsKey(realmChat2));
		assertFalse(actualUnreadChats.containsKey(realmChat3));
		assertEquals(Integer.valueOf(2), actualUnreadChats.get(realmChat4));
		assertEquals(Integer.valueOf(1), actualUnreadChats.get(realmChat1));
	}

	private ChatMessageImpl newMessage(String realmMessageId, boolean read) {
		final MessageImpl liteChatMessage = Messages.newMessage(testRealm.newMessageEntity(realmMessageId));
		liteChatMessage.setAuthor(testRealm.newUserEntity("user_01"));
		liteChatMessage.setRecipient(testRealm.newUserEntity("user_03"));
		liteChatMessage.setSendDate(DateTime.now());
		liteChatMessage.setBody(Strings.generateRandomString(10));
		return Messages.newChatMessage(liteChatMessage, read);
	}

	@Override
	public void tearDown() throws Exception {
		chatMessageDao.deleteAll();
		chatDao.deleteAll();
		super.tearDown();
	}
}
