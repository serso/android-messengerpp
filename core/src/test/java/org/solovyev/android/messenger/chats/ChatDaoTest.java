package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.DefaultDaoTest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.newPrivateChat;

public class ChatDaoTest extends DefaultDaoTest<Chat> {

	@Inject
	@Nonnull
	private SqliteChatDao dao;

	@Nonnull
	@Override
	protected Dao<Chat> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected Collection<Chat> populateEntities(@Nonnull Dao<Chat> dao) {
		final List<Chat> chats = new ArrayList<Chat>();
		for (int i = 0; i < 100; i++) {
			final Chat chat = newPrivateChat(getAccount1().newChatEntity("test_chat_" + i));
			chats.add(chat);
			dao.create(chat);
		}
		return chats;
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
