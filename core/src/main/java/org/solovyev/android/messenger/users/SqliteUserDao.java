package org.solovyev.android.messenger.users;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.accounts.DeleteAllRowsForAccountDbExec;
import org.solovyev.android.messenger.db.StringIdMapper;
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

	@Inject
	public SqliteUserDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		dao = new SqliteDao<User>("users", "id", new UserDaoMapper(this), context, sqliteOpenHelper);
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
	public List<AProperty> readUserPropertiesById(@Nonnull String userId) {
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
		throw new UnsupportedOperationException("Delete by id is not supported for user!");
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

	@Override
	public void deleteAllUsersForAccount(@Nonnull String accountId) {
		// todo serso: startWith must be replaced with equals!
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_contacts", "user_id", accountId));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_properties", "user_id", accountId));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_chats", "user_id", accountId));
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newInstance("users", "account_id", accountId));
	}

	@Nonnull
	@Override
	public List<String> readUserContactIds(@Nonnull String userId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadContactIdsByUserId(getContext(), userId, getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public List<User> readUserContacts(@Nonnull String userId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadContactsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
	}

	@Nonnull
	@Override
	public MergeDaoResult<User, String> mergeUserContacts(@Nonnull String userId, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResultImpl<User, String> result = new MergeDaoResultImpl<User, String>(contacts);

		final List<String> contactIdsFromDb = readUserContactIds(userId);
		for (final String contactIdFromDb : contactIdsFromDb) {
			try {
				// contact exists both in db and on remote server => just update contact properties
				final User updatedObject = Iterables.find(contacts, new UserByIdFinder(contactIdFromDb));
				if (allowUpdate) {
					result.addUpdatedObject(updatedObject);
				}
			} catch (NoSuchElementException e) {
				if (allowRemoval) {
					// contact was removed on remote server => need to remove from local db
					result.addRemovedObjectId(contactIdFromDb);
				}
			}
		}

		final Collection<String> userIdsFromDb = readAllIds();
		for (User contact : contacts) {
			try {
				// contact exists both in db and on remote server => case already covered above
				Iterables.find(contactIdsFromDb, Predicates.equalTo(contact.getEntity().getEntityId()));
			} catch (NoSuchElementException e) {
				// contact was added on remote server => need to add to local db
				if (userIdsFromDb.contains(contact.getEntity().getEntityId())) {
					// only link must be added - user already in users table
					result.addAddedObjectLink(contact);
				} else {
					// no user information in local db is available - full user insertion
					result.addAddedObject(contact);
				}
			}
		}

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
	public void updateUserOnlineStatus(@Nonnull User user) {
		doDbExec(getSqliteOpenHelper(), new InsertOrUpdateOnlineStatus(user));
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

	private static final class LoadContactIdsByUserId extends AbstractDbQuery<List<String>> {

		@Nonnull
		private final String userId;

		private LoadContactIdsByUserId(@Nonnull Context context, @Nonnull String userId, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.userId = userId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("users", null, "id in (select contact_id from user_contacts where user_id = ? )", new String[]{userId}, null, null, null);
		}

		@Nonnull
		@Override
		public List<String> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
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

	private static class UserByIdFinder implements Predicate<User> {

		@Nonnull
		private final String userId;

		public UserByIdFinder(@Nonnull String userId) {
			this.userId = userId;
		}

		@Override
		public boolean apply(@javax.annotation.Nullable User user) {
			return user != null && userId.equals(user.getEntity().getEntityId());
		}
	}

	private static class InsertOrUpdateOnlineStatus extends AbstractObjectDbExec<User> {

		protected InsertOrUpdateOnlineStatus(@Nonnull User object) {
			super(object);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			final User user = getNotNullObject();
			values.put("user_id", user.getId());
			values.put("property_name", User.PROPERTY_ONLINE);
			values.put("property_value", String.valueOf(user.isOnline()));
			return db.replace("user_properties", null, values);
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
