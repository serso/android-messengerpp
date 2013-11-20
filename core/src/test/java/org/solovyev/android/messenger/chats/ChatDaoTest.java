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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageDao;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.solovyev.android.messenger.chats.Chats.newPrivateChat;

public class ChatDaoTest extends DefaultDaoTest<Chat> {

	@Inject
	@Nonnull
	private ChatDao dao;

	@Inject
	@Nonnull
	private MessageDao messageDao;

	@Nonnull
	@Override
	protected Dao<Chat> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected String getId(Chat chat) {
		return chat.getId();
	}

	@Nonnull
	@Override
	protected Collection<Chat> populateEntities(@Nonnull Dao<Chat> dao) {
		final Collection<AccountChat> chats = getAllChats().values();
		return Collections2.transform(chats, new Function<AccountChat, Chat>() {
			@Override
			public Chat apply(AccountChat accountChat) {
				return accountChat.getChat();
			}
		});
	}

	private Map<String, AccountChat> getAllChats() {
		final Map<String, AccountChat> chats = new HashMap<String, AccountChat>();
		addChatsFromAccount(chats, getAccountData1());
		addChatsFromAccount(chats, getAccountData2());
		addChatsFromAccount(chats, getAccountData3());
		return chats;
	}

	@Nonnull
	private Map<String, AccountChat> addChatsFromAccount(@Nonnull Map<String, AccountChat> chats, @Nonnull AccountData ad) {
		for (AccountChat chat : ad.getChats()) {
			chats.put(chat.getChat().getId(), chat);
		}

		return chats;
	}

	@Test
	public void testMessagesShouldBeRemovedIfChatRemoved() throws Exception {
		final AccountChat chat = getAccountData1().getChats().get(0);
		assertFalse(messageDao.readMessages(chat.getChat().getId()).isEmpty());

		dao.deleteById(chat.getChat().getId());

		assertTrue(messageDao.readMessages(chat.getChat().getId()).isEmpty());
	}

	@Test
	public void testPropertiesShouldBeRemovedIfChatRemoved() throws Exception {
		final AccountChat chat = getAccountData1().getChats().get(0);
		assertFalse(dao.readPropertiesById(chat.getChat().getId()).isEmpty());

		dao.deleteById(chat.getChat().getId());

		assertTrue(dao.readPropertiesById(chat.getChat().getId()).isEmpty());
	}

	@Test
	public void testPropertiesShouldBeRemovedIfUserRemoved() throws Exception {
		final String chatId = getAccountData1().getChats().get(0).getChat().getId();

		assertFalse(dao.readPropertiesById(chatId).isEmpty());

		dao.deleteById(chatId);

		assertTrue(dao.readPropertiesById(chatId).isEmpty());
	}

	@Nonnull
	@Override
	protected DaoEntity<MutableChat> newInsertEntity() {
		return newEntity(newPrivateChat(getAccount1().newChatEntity("test_chat_1")));
	}

	@Nonnull
	@Override
	protected Chat changeEntity(@Nonnull Chat chat) {
		return chat;
	}

	@Test
	public void testShouldReadChatsMostRecentFirst() throws Exception {
		final List<String> chatIds = dao.readLastChatIds(null, false, Integer.MAX_VALUE);
		final Map<String, AccountChat> chats = getAllChats();
		checkChatsAreSortedRecentFirst(chats, chatIds);
		assertEquals(chats.size(), chatIds.size());
	}

	@Test
	public void testShouldReadPrivateChatsMostRecentFirst() throws Exception {
		final List<String> chatIds = dao.readLastChatIds(null, true, Integer.MAX_VALUE);
		final Map<String, AccountChat> chats = getAllChats();
		checkChatsAreSortedRecentFirst(chats, chatIds);
		assertEquals(chats.size(), chatIds.size());
	}

	@Test
	public void testShouldReadChatsMostRecentFirstWithLimit() throws Exception {
		final List<String> chatIds = dao.readLastChatIds(null, false, 10);
		final Map<String, AccountChat> chats = getAllChats();
		checkChatsAreSortedRecentFirst(chats, chatIds);
		assertEquals(10, chatIds.size());
	}

	@Test
	public void testShouldReadChatsMostRecentFirstWithLimitForUser() throws Exception {
		final AccountData ad = getAccountData3();
		final List<String> chatIds = dao.readLastChatIds(ad.getAccount().getUser().getId(), false, 10);
		final Map<String, AccountChat> chats = addChatsFromAccount(new HashMap<String, AccountChat>(), ad);
		checkChatsAreSortedRecentFirst(chats, chatIds);
		assertEquals(10, chatIds.size());
	}

	private void checkChatsAreSortedRecentFirst(@Nonnull Map<String, AccountChat> chats, @Nonnull List<String> chatIds) {
		AccountChat previousChat = null;
		for (String chatId : chatIds) {
			final AccountChat chat = chats.get(chatId);
			if (previousChat != null) {
				final Message previousFirstMessage = previousChat.getMessages().get(previousChat.getMessages().size() - 1);
				final Message firstMessage = chat.getMessages().get(chat.getMessages().size() - 1);
				assertTrue(!previousFirstMessage.getSendDate().isBefore(firstMessage.getSendDate()));
			}
			previousChat = chat;
		}
	}

	@Test
	public void testShouldLoadAllUnreadChats() throws Exception {
		final Map<Entity, Integer> unreadChats = dao.getUnreadChats();

		Map<String, AccountChat> chats = getAllChats();
		for (Map.Entry<Entity, Integer> entry : unreadChats.entrySet()) {
			final AccountChat chat = chats.get(entry.getKey().getEntityId());

			Integer unreadMessages = 0;
			for (Message message : chat.getMessages()) {
				if (!message.isRead()) {
					unreadMessages++;
				}
			}

			assertTrue(entry.getValue() > 0);
			assertEquals(unreadMessages, entry.getValue());
		}

	}
}
