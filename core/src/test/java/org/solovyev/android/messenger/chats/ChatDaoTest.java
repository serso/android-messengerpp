package org.solovyev.android.messenger.chats;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;
import org.solovyev.android.messenger.messages.ChatMessageDao;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.chats.Chats.newPrivateChat;

public class ChatDaoTest extends DefaultDaoTest<Chat> {

	@Inject
	@Nonnull
	private ChatDao dao;

	@Inject
	@Nonnull
	private ChatMessageDao chatMessageDao;

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
		final List<ApiChat> chats = new ArrayList<ApiChat>();
		chats.addAll(getAccountData1().getChats());
		chats.addAll(getAccountData2().getChats());
		chats.addAll(getAccountData3().getChats());
		return Collections2.transform(chats, new Function<ApiChat, Chat>() {
			@Override
			public Chat apply(@Nullable ApiChat apiChat) {
				return apiChat.getChat();
			}
		});
	}

	@Test
	public void testMessagesShouldBeRemovedIfChatRemoved() throws Exception {
		final ApiChat chat = getAccountData1().getChats().get(0);
		assertFalse(chatMessageDao.readMessages(chat.getChat().getId()).isEmpty());

		dao.deleteById(chat.getChat().getId());

		assertTrue(chatMessageDao.readMessages(chat.getChat().getId()).isEmpty());
	}

	@Test
	public void testPropertiesShouldBeRemovedIfChatRemoved() throws Exception {
		final ApiChat chat = getAccountData1().getChats().get(0);
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
	protected Entity<Chat> newInsertEntity() {
		return newEntity(newPrivateChat(getAccount1().newChatEntity("test_chat_1")));
	}

	@Nonnull
	@Override
	protected Chat changeEntity(@Nonnull Chat chat) {
		return chat;
	}
}
