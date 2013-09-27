package org.solovyev.android.messenger.messages;

import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.db.CommonSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.AppTest;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;

import com.google.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

public class ChatMessageDaoTest extends AbstractMessengerTestCase {

	@Inject
	@Nonnull
	private SqliteChatMessageDao dao;

	@Inject
	@Nonnull
	private SqliteChatDao chatDao;

	@Test
	public void testLastMessageShouldBeMessageWithLastDate() throws Exception {
		AppTest.mockApp();
		final List<ChatMessage> messages = new ArrayList<ChatMessage>();
		final DateTime now = DateTime.now();
		messages.add(newMockMessage(now));
		messages.add(newMockMessage(now.plusDays(1)));
		messages.add(newMockMessage(now.plusDays(2)));
		messages.add(newMockMessage(now.plusDays(4)));
		final ChatService chatService = mock(ChatService.class);
		final Chat chat = mock(Chat.class);
		when(chatService.getChatById(any(Entity.class))).thenReturn(chat);
		dao.mergeChatMessages("test" + EntityImpl.DELIMITER + "test", messages, false);
	}
}
