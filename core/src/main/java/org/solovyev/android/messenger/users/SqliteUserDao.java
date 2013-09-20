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
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.android.messenger.accounts.DeleteAllRowsForAccountDbExec;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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

	@Inject
	public SqliteUserDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	SqliteUserDao(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	@Nonnull
	@Override
	public User insertUser(@Nonnull User user) {
		final long result = AndroidDbUtils.doDbExec(getSqliteOpenHelper(), new InsertUser(user));
		if (result != DbExec.SQL_ERROR) {
			AndroidDbUtils.doDbExec(getSqliteOpenHelper(), new InsertUserProperties(user));
		}
		return user;
	}

	@Nullable
	@Override
	public User loadUserById(@Nonnull String userId) {
		final List<User> users = AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadByUserId(getContext(), userId, getSqliteOpenHelper(), this));
		return Collections.getFirstListElement(users);
	}

	@Nonnull
	@Override
	public List<AProperty> loadUserPropertiesById(@Nonnull String userId) {
		return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserPropertiesDbQuery(userId, getContext(), getSqliteOpenHelper()));
	}

	@Override
	public void updateUser(@Nonnull User user) {
		final long rows = AndroidDbUtils.doDbExec(getSqliteOpenHelper(), new UpdateUser(user));
		if (rows > 0) {
			// user exists => can remove/insert properties
			AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteUserProperties(user), new InsertUserProperties(user)));
		}
	}

	@Nonnull
	@Override
	public List<String> loadUserIds() {
		return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserIds(getContext(), getSqliteOpenHelper()));
	}

	@Override
	public void deleteAllUsers() {
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_contacts"));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_properties"));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("user_chats"));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("users"));
	}

	@Override
	public void deleteAllUsersInRealm(@Nonnull String realmId) {
		// todo serso: startWith must be replaced with equals!
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_contacts", "user_id", realmId));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_properties", "user_id", realmId));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newStartsWith("user_chats", "user_id", realmId));
		AndroidDbUtils.doDbExec(getSqliteOpenHelper(), DeleteAllRowsForAccountDbExec.newInstance("users", "realm_id", realmId));
	}

	@Nonnull
	@Override
	public List<String> loadUserContactIds(@Nonnull String userId) {
		return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadContactIdsByUserId(getContext(), userId, getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public List<User> loadUserContacts(@Nonnull String userId) {
		return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadContactsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
	}

	@Nonnull
	@Override
	public MergeDaoResult<User, String> mergeUserContacts(@Nonnull String userId, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResultImpl<User, String> result = new MergeDaoResultImpl<User, String>(contacts);

		final List<String> contactIdsFromDb = loadUserContactIds(userId);
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

		final List<String> userIdsFromDb = loadUserIds();
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

		AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), execs);

		return result;
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

	private static final class LoadUserIds extends AbstractDbQuery<List<String>> {

		private LoadUserIds(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("users", new String[]{"id"}, null, null, null, null, null);
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

	private static final class LoadByUserId extends AbstractDbQuery<List<User>> {

		@Nonnull
		private final String userId;

		@Nonnull
		private final UserDao userDao;

		private LoadByUserId(@Nonnull Context context,
							 @Nonnull String userId,
							 @Nonnull SQLiteOpenHelper sqliteOpenHelper,
							 @Nonnull UserDao userDao) {
			super(context, sqliteOpenHelper);
			this.userId = userId;
			this.userDao = userDao;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("users", null, "id = ? ", new String[]{userId}, null, null, null);
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

			for (AProperty property : user.getProperties()) {
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

	@Nonnull
	private static ContentValues toContentValues(@Nonnull User user) {
		final ContentValues values = new ContentValues();

		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
		final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();

		values.put("id", user.getEntity().getEntityId());
		values.put("realm_id", user.getEntity().getRealmId());
		values.put("realm_user_id", user.getEntity().getRealmEntityId());
		values.put("last_properties_sync_date", lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(lastPropertiesSyncDate));
		values.put("last_contacts_sync_date", lastContactsSyncDate == null ? null : dateTimeFormatter.print(lastContactsSyncDate));

		return values;
	}
}
