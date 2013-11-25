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
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.ReplacePropertyExec;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.solovyev.android.db.AndroidDbUtils.*;
import static org.solovyev.android.messenger.users.Users.newOnlineProperty;

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
		linkedEntitiesDao = new SqliteLinkedEntitiesDao<User>("users", "id", context, sqliteOpenHelper, "user_contacts", "user_id", "contact_id", dao);
	}

	@Override
	public long create(@Nonnull User user) {
		final long result = dao.create(user);
		if (result != DbExec.SQL_ERROR) {
			doDbExec(getSqliteOpenHelper(), new InsertProperties(user));
		}
		return result;
	}

	@Override
	public long createContact(@Nonnull String userId, @Nonnull User contact) {
		final long result = dao.create(contact);
		if (result != DbExec.SQL_ERROR) {
			doDbExec(getSqliteOpenHelper(), new InsertProperties(contact));
			doDbExec(getSqliteOpenHelper(), new InsertContact(userId, contact.getId()));
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
		return doDbQuery(getSqliteOpenHelper(), new LoadPropertiesDbQuery(userId, getContext(), getSqliteOpenHelper()));
	}

	@Override
	public long update(@Nonnull User user) {
		final long rows = dao.update(user);
		if (rows > 0) {
			// user exists => can remove/insert properties
			doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteProperties(user), new InsertProperties(user)));
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
			execs.add(new DeleteProperties(updatedContact));
			execs.add(new InsertProperties(updatedContact));
		}

		for (User addedContact : result.getAddedObjects()) {
			execs.add(new InsertUser(addedContact));
			execs.add(new InsertProperties(addedContact));
			execs.add(new InsertContact(userId, addedContact.getEntity().getEntityId()));
		}

		doDbExecs(getSqliteOpenHelper(), execs);

		return result;
	}

	@Override
	public void updateOnlineStatus(@Nonnull User user) {
		doDbExec(getSqliteOpenHelper(), newReplacePropertyExec(user, newOnlineProperty(user.isOnline())));
	}

	@Nonnull
	private ReplacePropertyExec newReplacePropertyExec(@Nonnull User user,
													   @Nonnull AProperty property) {
		return new ReplacePropertyExec(user, "user_properties", "user_id", property.getName(), property.getValue());
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

	private static final class LoadPropertiesDbQuery extends PropertyByIdDbQuery {

		public LoadPropertiesDbQuery(@Nonnull String userId, @Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
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

	private static final class DeleteProperties extends AbstractObjectDbExec<User> {

		private DeleteProperties(@Nonnull User user) {
			super(user);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final User user = getNotNullObject();

			return db.delete("user_properties", "user_id = ?", new String[]{String.valueOf(user.getEntity().getEntityId())});
		}
	}

	private static final class InsertProperties extends AbstractObjectDbExec<User> {

		private InsertProperties(@Nonnull User user) {
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

		values.put("id", user.getEntity().getEntityId());
		values.put("account_id", user.getEntity().getAccountId());
		values.put("account_user_id", user.getEntity().getAccountEntityId());

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
	}

}
