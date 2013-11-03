package org.solovyev.android.messenger.chats;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.SqliteMessageDao;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.*;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static org.solovyev.android.db.AndroidDbUtils.*;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:27 PM
 */
@Singleton
public class SqliteChatDao extends AbstractSQLiteHelper implements ChatDao {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private UserService userService;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final Dao<Chat> dao;

	@Nonnull
	private final LinkedEntitiesDao<Chat> linkedEntitiesDao;

	@Inject
	public SqliteChatDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		final ChatDaoMapper chatDaoMapper = new ChatDaoMapper(this);
		dao = new SqliteDao<Chat>("chats", "id", chatDaoMapper, context, sqliteOpenHelper);
		linkedEntitiesDao = new SqliteLinkedEntitiesDao<Chat>("chats", "id", context, sqliteOpenHelper, "user_chats", "user_id", "chat_id", dao);
	}

	@Nonnull
	@Override
	public Collection<String> readLinkedEntityIds(@Nonnull String userId) {
		return linkedEntitiesDao.readLinkedEntityIds(userId);
	}

	@Override
	public long update(@Nonnull Chat chat) {
		final long rows = dao.update(chat);
		if (rows >= 0) {
			doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteChatProperties(chat), new InsertChatProperties(chat)));
		}

		return rows;
	}

	@Override
	public void deleteAll() {
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_chats"));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("chat_properties"));
		dao.deleteAll();
	}

	@Nonnull
	@Override
	public Map<Entity, Integer> getUnreadChats() {
		return doDbQuery(getSqliteOpenHelper(), new UnreadChatsLoader(getContext(), getSqliteOpenHelper()));
	}

	@Override
	public void delete(@Nonnull User user, @Nonnull Chat chat) {
		doDbExec(getSqliteOpenHelper(), new RemoveChats(user.getId(), chat));
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Nonnull
	@Override
	public List<AProperty> readPropertiesById(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadChatPropertiesDbQuery(chatId, getContext(), getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public List<Chat> readChatsByUserId(@Nonnull String userId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadChatsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
	}

	@Nonnull
	@Override
	public List<User> readParticipants(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadChatParticipants(getContext(), chatId, userService, getSqliteOpenHelper()));
	}

	@Override
	public Chat read(@Nonnull String chatId) {
		return dao.read(chatId);
	}

	@Nonnull
	@Override
	public Collection<Chat> readAll() {
		return dao.readAll();
	}

	@Override
	public long create(@Nonnull Chat chat) {
		return dao.create(chat);
	}

	@Override
	public void delete(@Nonnull Chat chat) {
		deleteById(chat.getId());
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}

	private static final class LoadChatParticipants extends AbstractDbQuery<List<User>> {

		@Nonnull
		private final String chatId;

		@Nonnull
		private final UserService userService;

		private LoadChatParticipants(@Nonnull Context context,
									 @Nonnull String chatId,
									 @Nonnull UserService userService,
									 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
			this.userService = userService;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("user_chats", null, "chat_id = ? ", new String[]{chatId}, null, null, null);
		}

		@Nonnull
		@Override
		public List<User> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<User>(new ChatParticipantMapper(userService)).convert(cursor);
		}
	}

	private static final class LoadChatsByUserId extends AbstractDbQuery<List<Chat>> {

		@Nonnull
		private final String userId;

		@Nonnull
		private final ChatDao chatDao;

		private LoadChatsByUserId(@Nonnull Context context, @Nonnull String userId, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull ChatDao chatDao) {
			super(context, sqliteOpenHelper);
			this.userId = userId;
			this.chatDao = chatDao;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("chats", null, "id in (select chat_id from user_chats where user_id = ? ) ", new String[]{userId}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Chat> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Chat>(new ChatMapper(chatDao)).convert(cursor);
		}
	}

	public static final class LoadChatPropertiesDbQuery extends PropertyByIdDbQuery {

		public LoadChatPropertiesDbQuery(@Nonnull String chatId, @Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper, "chat_properties", "chat_id", chatId);
		}
	}

	@Nonnull
	@Override
	public MergeDaoResult<Chat, String> mergeChats(@Nonnull String userId, @Nonnull Iterable<? extends AccountChat> chats) {
		final MergeDaoResult<Chat, String> result = mergeLinkedEntities(userId, chats);

		final List<DbExec> execs = new ArrayList<DbExec>();

		for (final Chat addedChat : result.getAddedObjects()) {
			final AccountChat chat = find(chats, new Predicate<AccountChat>() {
				@Override
				public boolean apply(AccountChat chat) {
					return chat.getChat().equals(addedChat);
				}
			});

			for (Message message : chat.getMessages()) {
				execs.add(new SqliteMessageDao.InsertMessage(message));
			}

			for (User participant : chat.getParticipants()) {
				final String participantId = participant.getId();
				if (!participantId.equals(userId)) {
					execs.add(new InsertChatLink(participantId, addedChat.getId()));
				}
			}
		}

		doDbExecs(getSqliteOpenHelper(), execs);

		return result;
	}

	private MergeDaoResult<Chat, String> mergeLinkedEntities(@Nonnull String userId, Iterable<? extends AccountChat> apiChats) {
		// !!! actually not all chats are loaded and we cannot delete the chat just because it is not in the list
		return mergeLinkedEntities(userId, transform(apiChats, new Function<AccountChat, Chat>() {
			@Override
			public Chat apply(@Nullable AccountChat accountChat) {
				assert accountChat != null;
				return accountChat.getChat();
			}
		}), false, true);
	}

	@Nonnull
	@Override
	public MergeDaoResult<Chat, String> mergeLinkedEntities(@Nonnull String userId, @Nonnull Iterable<Chat> linkedEntities, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResult<Chat, String> result = linkedEntitiesDao.mergeLinkedEntities(userId, linkedEntities, allowRemoval, allowUpdate);

		final List<DbExec> execs = new ArrayList<DbExec>();

		if (!result.getRemovedObjectIds().isEmpty()) {
			execs.addAll(RemoveChats.newInstances(userId, result.getRemovedObjectIds()));
		}

		for (Chat updatedChat : result.getUpdatedObjects()) {
			execs.add(new UpdateChat(updatedChat));
			execs.add(new DeleteChatProperties(updatedChat));
			execs.add(new InsertChatProperties(updatedChat));
		}

		for (final Chat addedChat : result.getAddedObjects()) {
			execs.add(new InsertChat(addedChat));
			execs.add(new InsertChatProperties(addedChat));
			execs.add(new InsertChatLink(userId, addedChat.getEntity().getEntityId()));
		}

		doDbExecs(getSqliteOpenHelper(), execs);

		return result;
	}

	private static final class RemoveChats implements DbExec {

		@Nonnull
		private String userId;

		@Nonnull
		private List<String> chatIds;

		private RemoveChats(@Nonnull String userId, @Nonnull List<String> chatIds) {
			this.userId = userId;
			this.chatIds = chatIds;
		}

		private RemoveChats(@Nonnull String userId, @Nonnull Chat chat) {
			this.userId = userId;
			this.chatIds = Arrays.asList(chat.getId());
		}

		@Nonnull
		private static List<RemoveChats> newInstances(@Nonnull String userId, @Nonnull List<String> chatIds) {
			final List<RemoveChats> result = new ArrayList<RemoveChats>();

			for (List<String> chatIdsChunk : Collections.split(chatIds, MAX_IN_COUNT)) {
				result.add(new RemoveChats(userId, chatIdsChunk));
			}

			return result;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			return db.delete("user_chats", "user_id = ? and chat_id in " + AndroidDbUtils.inClause(chatIds), AndroidDbUtils.inClauseValues(chatIds, userId));
		}
	}


	private static final class UpdateChat extends AbstractObjectDbExec<Chat> {

		private UpdateChat(@Nonnull Chat chat) {
			super(chat);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Chat chat = getNotNullObject();

			final ContentValues values = toContentValues(chat);

			return db.update("chats", values, "id = ?", new String[]{String.valueOf(chat.getEntity().getEntityId())});
		}
	}

	private static final class InsertChat extends AbstractObjectDbExec<Chat> {

		private InsertChat(@Nonnull Chat chat) {
			super(chat);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Chat chat = getNotNullObject();

			final ContentValues values = toContentValues(chat);

			return db.insert("chats", null, values);
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull Chat chat) {
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		final DateTime lastMessagesSyncDate = chat.getLastMessagesSyncDate();

		final ContentValues values = new ContentValues();

		values.put("id", chat.getEntity().getEntityId());
		values.put("account_id", chat.getEntity().getAccountId());
		values.put("account_chat_id", chat.getEntity().getAccountEntityId());
		values.put("last_messages_sync_date", lastMessagesSyncDate == null ? null : dateTimeFormatter.print(lastMessagesSyncDate));

		return values;
	}

	private static final class DeleteChatProperties extends AbstractObjectDbExec<Chat> {

		private DeleteChatProperties(@Nonnull Chat chat) {
			super(chat);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Chat chat = getNotNullObject();

			return db.delete("chat_properties", "chat_id = ?", new String[]{String.valueOf(chat.getEntity().getEntityId())});
		}
	}

	private static final class InsertChatProperties extends AbstractObjectDbExec<Chat> {

		private InsertChatProperties(@Nonnull Chat chat) {
			super(chat);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			long result = 0;
			final Chat chat = getNotNullObject();

			for (AProperty property : chat.getPropertiesCollection()) {
				final ContentValues values = new ContentValues();
				values.put("chat_id", chat.getEntity().getEntityId());
				values.put("property_name", property.getName());
				values.put("property_value", property.getValue());
				final long id = db.insert("chat_properties", null, values);
				if (id == SQL_ERROR) {
					result = SQL_ERROR;
				}
			}

			return result;
		}
	}

	private static final class InsertChatLink implements DbExec {

		@Nonnull
		private String userId;

		@Nonnull
		private String chatId;

		private InsertChatLink(@Nonnull String userId, @Nonnull String chatId) {
			this.userId = userId;
			this.chatId = chatId;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			values.put("user_id", userId);
			values.put("chat_id", chatId);
			return db.insert("user_chats", null, values);
		}
	}

	private static final class UnreadChatsLoader extends AbstractDbQuery<Map<Entity, Integer>> {

		protected UnreadChatsLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery(	"select c.id, c.account_id, c.account_chat_id, count(*) from chats c, messages m " +
								"where c.id = m.chat_id " +
								"and m.read = 0 " +
								"and m.state = 'received' " +
								"group by c.id, c.account_id, c.account_chat_id", null);
		}

		@Nonnull
		@Override
		public Map<Entity, Integer> retrieveData(@Nonnull Cursor cursor) {
			final Map<Entity, Integer> result = new HashMap<Entity, Integer>();

			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					final int unreadMessagesCount = cursor.getInt(3);
					if (unreadMessagesCount > 0) {
						result.put(EntityMapper.newInstanceFor(0).convert(cursor), unreadMessagesCount);
					}
					cursor.moveToNext();
				}
			}

			return result;
		}
	}

	private static final class ChatDaoMapper implements SqliteDaoEntityMapper<Chat> {

		@Nonnull
		private final ChatMapper chatMapper;

		private ChatDaoMapper(@Nonnull ChatDao dao) {
			chatMapper = new ChatMapper(dao);
		}

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull Chat chat) {
			return SqliteChatDao.toContentValues(chat);
		}

		@Nonnull
		@Override
		public Converter<Cursor, Chat> getCursorMapper() {
			return chatMapper;
		}
	}
}
