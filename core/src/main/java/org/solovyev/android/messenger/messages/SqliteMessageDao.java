package org.solovyev.android.messenger.messages;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getFirst;
import static org.solovyev.android.db.AndroidDbUtils.*;
import static org.solovyev.android.messenger.messages.MessageState.removed;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:41 PM
 */
@Singleton
public class SqliteMessageDao extends AbstractSQLiteHelper implements MessageDao {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private ChatService chatService;

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
	private final Dao<Message> dao;

	@Inject
	public SqliteMessageDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		this.dao = new SqliteDao<Message>("messages", "id", new MessageDaoMapper(), context, sqliteOpenHelper);
	}

	@Nonnull
	@Override
	public List<String> readMessageIds(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadMessageIdsByChatId(getContext(), chatId, getSqliteOpenHelper()));
	}

	@Override
	public long create(@Nonnull Message message) {
		final long result = dao.create(message);
		if (result != DbExec.SQL_ERROR) {
			doDbExec(getSqliteOpenHelper(), new InsertProperties(message));
		}
		return result;
	}

	@Nullable
	@Override
	public Message read(@Nonnull String messageId) {
		return dao.read(messageId);
	}

	@Nonnull
	@Override
	public Collection<Message> readAll() {
		return dao.readAll();
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public long update(@Nonnull Message message) {
		final long rows = dao.update(message);
		if (rows > 0) {
			// message exists => can remove/insert properties
			doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteProperties(message), new InsertProperties(message)));
		}
		return rows;
	}

	@Override
	public void delete(@Nonnull Message message) {
		dao.delete(message);
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}

	@Nonnull
	@Override
	public List<Message> readMessages(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadMessages(getContext(), chatId, getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public String getOldestMessageForChat(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new OldestMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
	}

	@Nullable
	@Override
	public Message readLastMessage(@Nonnull String chatId) {
		final String lastMessageId = doDbQuery(getSqliteOpenHelper(), new LastMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
		if (!Strings.isEmpty(lastMessageId)) {
			final List<Message> messages = doDbQuery(getSqliteOpenHelper(), new LoadMessage(getContext(), lastMessageId, getSqliteOpenHelper()));
			return getFirst(messages, null);
		} else {
			return null;
		}
	}

	@Override
	public int getUnreadMessagesCount() {
		return doDbQuery(getSqliteOpenHelper(), new UnreadMessagesCountLoader(getContext(), getSqliteOpenHelper()));
	}

	@Override
	public boolean changeReadStatus(@Nonnull String messageId, boolean read) {
		final Long rows = doDbExec(getSqliteOpenHelper(), new ReadStatusUpdater(messageId, read));
		return rows != 0;
	}

	@Override
	public boolean changeMessageState(@Nonnull String messageId, @Nonnull MessageState state) {
		final Long rows = doDbExec(getSqliteOpenHelper(), new StateUpdater(messageId, state));
		return rows != 0;
	}

	@Override
	public void deleteAll() {
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("messages"));
	}

	@Nonnull
	@Override
	public List<AProperty> readPropertiesById(@Nonnull String messageId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadPropertiesDbQuery(messageId, getContext(), getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public MergeDaoResult<Message, String> mergeMessages(@Nonnull String chatId, @Nonnull Collection<? extends Message> messages, boolean allowDelete) {
		final MergeDaoResultImpl<Message, String> result = new MergeDaoResultImpl<Message, String>();

		final Chat chat = getChatService().getChatById(Entities.newEntityFromEntityId(chatId));

		if (chat != null) {
			final List<String> messageIdsFromDb = readMessageIds(chatId);
			for (final String messageIdFromDb : messageIdsFromDb) {
				try {
					// message exists both in db and on remote server => just update message properties
					result.addUpdatedObject(find(messages, new MessageByIdFinder(messageIdFromDb)));
				} catch (NoSuchElementException e) {
					// message was removed on remote server => need to remove from local db
					result.addRemovedObjectId(messageIdFromDb);
				}
			}

			for (Message message : messages) {
				try {
					// message exists both in db and on remote server => case already covered above
					find(messageIdsFromDb, equalTo(message.getEntity().getEntityId()));
				} catch (NoSuchElementException e) {
					// message was added on remote server => need to add to local db
					if (!messageIdsFromDb.contains(message.getEntity().getEntityId())) {
						// no message information in local db is available - full message insertion
						result.addAddedObject(message);
					}
				}
			}

			final List<DbExec> execs = new ArrayList<DbExec>();

			if (allowDelete) {
				if (!result.getRemovedObjectIds().isEmpty()) {
					execs.addAll(RemoveMessages.newInstances(result.getRemovedObjectIds()));
				}
			}

			for (Message updatedMessage : result.getUpdatedObjects()) {
				execs.add(new UpdateMessage(updatedMessage));
				execs.add(new DeleteProperties(updatedMessage));
				execs.add(new InsertProperties(updatedMessage));
			}

			for (Message addedMessage : result.getAddedObjects()) {
				execs.add(new InsertMessage(addedMessage));
				execs.add(new InsertProperties(addedMessage));
			}

			doDbExecs(getSqliteOpenHelper(), execs);
		}

		return result;
	}

	@Nonnull
	private ChatService getChatService() {
		return this.chatService;
	}

	public void setChatService(@Nonnull ChatService chatService) {
		this.chatService = chatService;
	}

	public void setUserService(@Nonnull UserService userService) {
		this.userService = userService;
	}

	/*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class LoadMessageIdsByChatId extends AbstractDbQuery<List<String>> {

		@Nonnull
		private final String chatId;

		private LoadMessageIdsByChatId(@Nonnull Context context, @Nonnull String chatId, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "chat_id = ? and state <> ?", new String[]{chatId, removed.name()}, null, null, null);
		}

		@Nonnull
		@Override
		public List<String> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
		}
	}

	private static class MessageByIdFinder implements Predicate<Message> {

		@Nonnull
		private final String messageId;

		public MessageByIdFinder(@Nonnull String messageId) {
			this.messageId = messageId;
		}

		@Override
		public boolean apply(@javax.annotation.Nullable Message message) {
			return message != null && message.getEntity().getEntityId().equals(messageId);
		}
	}

	public static final class InsertMessage extends AbstractObjectDbExec<Message> {

		public InsertMessage(@Nullable Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			final ContentValues values = toContentValues(message);

			return db.insert("messages", null, values);
		}
	}

	private static final class UpdateMessage extends AbstractObjectDbExec<Message> {

		private UpdateMessage(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			final ContentValues values = toContentValues(message);

			return db.update("messages", values, "id = ?", new String[]{String.valueOf(message.getEntity().getEntityId())});
		}
	}

	private static final class RemoveMessages implements DbExec {

		@Nonnull
		private List<String> messagesIds;

		private RemoveMessages(@Nonnull List<String> messagesIds) {
			this.messagesIds = messagesIds;
		}

		@Nonnull
		private static List<RemoveMessages> newInstances(@Nonnull List<String> messagesIds) {
			final List<RemoveMessages> result = new ArrayList<RemoveMessages>();

			for (List<String> messagesIdsChunk : Collections.split(messagesIds, MAX_IN_COUNT)) {
				result.add(new RemoveMessages(messagesIdsChunk));
			}

			return result;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			return db.delete("messages", "chat_id in " + inClause(messagesIds), inClauseValues(messagesIds));
		}
	}

	private class MessageDaoMapper implements SqliteDaoEntityMapper<Message> {

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull Message message) {
			return SqliteMessageDao.toContentValues(message);
		}

		@Nonnull
		@Override
		public Converter<Cursor, Message> getCursorMapper() {
			return new MessageMapper(SqliteMessageDao.this);
		}

		@Nonnull
		@Override
		public String getId(@Nonnull Message message) {
			return message.getId();
		}
	}


	private final class LoadMessages extends AbstractDbQuery<List<Message>> {

		@Nonnull
		private final String chatId;

		private LoadMessages(@Nonnull Context context,
							 @Nonnull String chatId,
							 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "chat_id = ? and state <> ?", new String[]{chatId, removed.name()}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Message> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Message>(new MessageMapper(SqliteMessageDao.this)).convert(cursor);
		}
	}

	private final class LoadMessage extends AbstractDbQuery<List<Message>> {

		@Nonnull
		private final String messageId;

		private LoadMessage(@Nonnull Context context,
							@Nonnull String messageId,
							@Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.messageId = messageId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "id = ? ", new String[]{String.valueOf(messageId)}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Message> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Message>(new MessageMapper(SqliteMessageDao.this)).convert(cursor);
		}
	}

	private static class OldestMessageLoader extends AbstractDbQuery<String> {

		@Nonnull
		private String chatId;

		protected OldestMessageLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull String chatId) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select id from messages where chat_id = ? and state <> ? order by send_time asc", new String[]{chatId, removed.name()});
		}

		@Nonnull
		@Override
		public String retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			} else {
				return "";
			}
		}
	}

	private static class LastMessageLoader extends AbstractDbQuery<String> {

		@Nonnull
		private String chatId;

		protected LastMessageLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull String chatId) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select id from messages where chat_id = ? and state <> ? order by send_time desc", new String[]{chatId, removed.name()});
		}

		@Nonnull
		@Override
		public String retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			} else {
				return "";
			}
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull Message message) {
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		final ContentValues values = new ContentValues();

		final Entity entity = message.getEntity();
		values.put("id", entity.getEntityId());
		values.put("account_id", entity.getAccountId());
		values.put("realm_message_id", entity.getAccountEntityId());

		values.put("chat_id", message.getChat().getEntityId());
		values.put("author_id", message.getAuthor().getEntityId());
		final Entity recipient = message.getRecipient();
		values.put("recipient_id", recipient == null ? null : recipient.getEntityId());
		values.put("send_date", dateTimeFormatter.print(message.getSendDate()));
		values.put("send_time", message.getSendDate().getMillis());
		values.put("title", message.getTitle());
		values.put("body", message.getBody());
		values.put("read", message.isRead() ? 1 : 0);
		values.put("state", message.getState().name());
		return values;
	}

	private static class UnreadMessagesCountLoader extends AbstractDbQuery<Integer> {

		private UnreadMessagesCountLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select count(*) from messages where read = 0 and and state <> ?", new String[]{removed.name()});
		}

		@Nonnull
		@Override
		public Integer retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			} else {
				return 0;
			}
		}
	}

	private static class ReadStatusUpdater implements DbExec {

		@Nonnull
		private final String messageId;

		private final boolean read;

		private ReadStatusUpdater(@Nonnull String messageId, boolean read) {
			this.messageId = messageId;
			this.read = read;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			final int newReadValue = read ? 1 : 0;
			values.put("read", newReadValue);
			return db.update("messages", values, "id = ? and read <> ?", new String[]{messageId, String.valueOf(newReadValue)});
		}
	}

	private static class StateUpdater implements DbExec {

		@Nonnull
		private final String messageId;

		@Nonnull
		private final MessageState state;

		private StateUpdater(@Nonnull String messageId, @Nonnull MessageState state) {
			this.messageId = messageId;
			this.state = state;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			values.put("state", state.name());
			return db.update("messages", values, "id = ?", new String[]{messageId});
		}
	}

	private static final class LoadPropertiesDbQuery extends PropertyByIdDbQuery {

		public LoadPropertiesDbQuery(@Nonnull String messageId, @Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper, "message_properties", "message_id", messageId);
		}
	}

	private static final class DeleteProperties extends AbstractObjectDbExec<Message> {

		private DeleteProperties(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			return db.delete("message_properties", "message_id = ?", new String[]{String.valueOf(message.getEntity().getEntityId())});
		}
	}

	private static final class InsertProperties extends AbstractObjectDbExec<Message> {

		private InsertProperties(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			long result = 0;

			final Message message = getNotNullObject();

			for (AProperty property : message.getProperties().getPropertiesCollection()) {
				final ContentValues values = new ContentValues();
				final String value = property.getValue();
				if (value != null) {
					values.put("message_id", message.getEntity().getEntityId());
					values.put("property_name", property.getName());
					values.put("property_value", value);
					final long id = db.insert("message_properties", null, values);
					if (id == DbExec.SQL_ERROR) {
						result = DbExec.SQL_ERROR;
					}
				}
			}

			return result;
		}
	}
}
