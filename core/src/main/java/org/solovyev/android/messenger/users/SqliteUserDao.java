package org.solovyev.android.messenger.users;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.ReplacePropertyExec;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Singleton;
import java.util.*;

import static org.solovyev.android.db.AndroidDbUtils.doDbExec;
import static org.solovyev.android.db.AndroidDbUtils.doDbExecs;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 2:13 AM
 */

/**
 * This class must be synchronized in outer scope
 */
@NotThreadSafe
@Singleton
public final class SqliteUserDao extends AbstractSQLiteHelper implements UserDao {

	@Nonnull
	private final Dao<User> dao;

	@Nonnull
	private final LinkedEntitiesDao<User> linkedEntitiesDao;

	@Inject
	public SqliteUserDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		final UserDaoMapper userDaoMapper = new UserDaoMapper(this);
		dao = new SqliteDao<User>("users", "id", userDaoMapper, context, sqliteOpenHelper);
		linkedEntitiesDao = new SqliteLinkedEntitiesDao<User>("users", "id", userDaoMapper, context, sqliteOpenHelper, "user_contacts", "user_id", "contact_id", dao, false);
	}

	@Override
	public long create(@Nonnull User user) {
		final long result = dao.create(user);
		if (result != DbExec.SQL_ERROR) {
			doDbExec(getSqliteOpenHelper(), new InsertUserProperties(user));
		}
		return result;
	}

	@Nullable
	@Override
	public User read(@Nonnull String userId) {
		return dao.read(userId);
	}

	@Nonnull
	@Override
	public Collection<User> readAll() {
		return dao.readAll();
	}

	@Nonnull
	@Override
	public List<AProperty> readPropertiesById(@Nonnull String userId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadUserPropertiesDbQuery(userId, getContext(), getSqliteOpenHelper()));
	}

	@Override
	public long update(@Nonnull User user) {
		final long rows = dao.update(user);
		if (rows > 0) {
			// user exists => can remove/insert properties
			doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteUserProperties(user), new InsertUserProperties(user)));
		}
		return rows;
	}

	@Override
	public void delete(@Nonnull User user) {
		deleteById(user.getId());
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public void deleteAll() {
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_contacts"));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_properties"));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_chats"));
		dao.deleteAll();
	}

	@Nonnull
	@Override
	public Collection<String> readLinkedEntityIds(@Nonnull String userId) {
		return linkedEntitiesDao.readLinkedEntityIds(userId);
	}

	@Nonnull
	@Override
	public List<User> readContacts(@Nonnull String userId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadContactsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
	}

	@Nonnull
	@Override
	public MergeDaoResult<User, String> mergeLinkedEntities(@Nonnull String userId, @Nonnull Iterable<User> contacts, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResult<User, String> result = linkedEntitiesDao.mergeLinkedEntities(userId, contacts, allowRemoval, allowUpdate);

		final List<DbExec> execs = new ArrayList<DbExec>();

		if (!result.getRemovedObjectIds().isEmpty()) {
			execs.addAll(RemoveContacts.newInstances(userId, result.getRemovedObjectIds()));
		}

		for (User updatedContact : result.getUpdatedObjects()) {
			execs.add(new UpdateUser(updatedContact));
			execs.add(new DeleteUserProperties(updatedContact));
			execs.add(new InsertUserProperties(updatedContact));
		}

		for (User addedContactLink : result.getAddedObjectLinks()) {
			execs.add(new UpdateUser(addedContactLink));
			execs.add(new DeleteUserProperties(addedContactLink));
			execs.add(new InsertUserProperties(addedContactLink));
			execs.add(new InsertContact(userId, addedContactLink.getEntity().getEntityId()));
		}


		for (User addedContact : result.getAddedObjects()) {
			execs.add(new InsertUser(addedContact));
			execs.add(new InsertUserProperties(addedContact));
			execs.add(new InsertContact(userId, addedContact.getEntity().getEntityId()));
		}

		doDbExecs(getSqliteOpenHelper(), execs);

		return result;
	}

	@Override
	public void updateOnlineStatus(@Nonnull User user) {
		doDbExec(getSqliteOpenHelper(), newReplacePropertyExec(user, User.PROPERTY_ONLINE, String.valueOf(user.isOnline())));
	}

	@Nonnull
	private ReplacePropertyExec newReplacePropertyExec(@Nonnull User user,
															  @Nonnull String propertyName,
															  @Nonnull String propertyValue) {
		return new ReplacePropertyExec(user, "user_properties", "user_id", propertyName, propertyValue);
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */


	private static final class InsertContact implements DbExec {

		@Nonnull
		private String userId;

		@Nonnull
		private String contactId;

		private InsertContact(@Nonnull String userId, @Nonnull String contactId) {
			this.userId = userId;
			this.contactId = contactId;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			values.put("user_id", userId);
			values.put("contact_id", contactId);
			return db.insert("user_contacts", null, values);
		}
	}

	private static final class RemoveContacts implements DbExec {

		@Nonnull
		private String userId;

		@Nonnull
		private List<String> contactIds;

		private RemoveContacts(@Nonnull String userId, @Nonnull List<String> contactIds) {
			this.userId = userId;
			this.contactIds = contactIds;
		}

		@Nonnull
		private static List<RemoveContacts> newInstances(@Nonnull String userId, @Nonnull List<String> contactIds) {
			final List<RemoveContacts> result = new ArrayList<RemoveContacts>();

			for (List<String> contactIdsChunk : Collections.split(contactIds, MAX_IN_COUNT)) {
				result.add(new RemoveContacts(userId, contactIdsChunk));
			}

			return result;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			return db.delete("user_contacts", "user_id = ? and contact_id in " + AndroidDbUtils.inClause(contactIds), AndroidDbUtils.inClauseValues(contactIds, userId));
		}
	}

	private static final class LoadContactsByUserId extends AbstractDbQuery<List<User>> {

		@Nonnull
		private final String userId;

		@Nonnull
		private final UserDao userDao;

		private LoadContactsByUserId(@Nonnull Context context, @Nonnull String userId, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull UserDao userDao) {
			super(context, sqliteOpenHelper);
			this.userId = userId;
			this.userDao = userDao;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("users", null, "id in (select contact_id from user_contacts where user_id = ? ) ", new String[]{userId}, null, null, null);
		}

		@Nonnull
		@Override
		public List<User> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<User>(new UserMapper(userDao)).convert(cursor);
		}
	}

	public static final class LoadUserPropertiesDbQuery extends PropertyByIdDbQuery {

		public LoadUserPropertiesDbQuery(@Nonnull String userId, @Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper, "user_properties", "user_id", userId);
		}
	}

	private static final class InsertUser extends AbstractObjectDbExec<User> {

		private InsertUser(@Nonnull User user) {
			super(user);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final User user = getNotNullObject();

			final ContentValues values = toContentValues(user);

			return db.insert("users", null, values);
		}
	}


	private static final class UpdateUser extends AbstractObjectDbExec<User> {

		private UpdateUser(@Nonnull User user) {
			super(user);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final User user = getNotNullObject();

			final ContentValues values = toContentValues(user);

			return db.update("users", values, "id = ?", new String[]{String.valueOf(user.getEntity().getEntityId())});
		}
	}

	private static final class DeleteUserProperties extends AbstractObjectDbExec<User> {

		private DeleteUserProperties(@Nonnull User user) {
			super(user);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final User user = getNotNullObject();

			return db.delete("user_properties", "user_id = ?", new String[]{String.valueOf(user.getEntity().getEntityId())});
		}
	}

	private static final class InsertUserProperties extends AbstractObjectDbExec<User> {

		private InsertUserProperties(@Nonnull User user) {
			super(user);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			long result = 0;

			final User user = getNotNullObject();

			for (AProperty property : user.getPropertiesCollection()) {
				final ContentValues values = new ContentValues();
				final String value = property.getValue();
				if (value != null) {
					values.put("user_id", user.getEntity().getEntityId());
					values.put("property_name", property.getName());
					values.put("property_value", value);
					final long id = db.insert("user_properties", null, values);
					if (id == DbExec.SQL_ERROR) {
						result = DbExec.SQL_ERROR;
					}
				}
			}

			return result;
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull User user) {
		final ContentValues values = new ContentValues();

		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
		final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();

		values.put("id", user.getEntity().getEntityId());
		values.put("account_id", user.getEntity().getAccountId());
		values.put("realm_user_id", user.getEntity().getAccountEntityId());
		values.put("last_properties_sync_date", lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(lastPropertiesSyncDate));
		values.put("last_contacts_sync_date", lastContactsSyncDate == null ? null : dateTimeFormatter.print(lastContactsSyncDate));

		return values;
	}

	private static final class UserDaoMapper implements SqliteDaoEntityMapper<User> {

		@Nonnull
		private final UserMapper userMapper;

		private UserDaoMapper(@Nonnull UserDao dao) {
			userMapper = new UserMapper(dao);
		}

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull User user) {
			return SqliteUserDao.toContentValues(user);
		}

		@Nonnull
		@Override
		public Converter<Cursor, User> getCursorMapper() {
			return userMapper;
		}

		@Nonnull
		@Override
		public String getId(@Nonnull User user) {
			return user.getId();
		}
	}

}
