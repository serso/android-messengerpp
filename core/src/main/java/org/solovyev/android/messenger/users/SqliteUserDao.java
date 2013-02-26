package org.solovyev.android.messenger.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.common.collections.Collections;
import roboguice.inject.ContextSingleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 2:13 AM
 */
@ContextSingleton
public class SqliteUserDao extends AbstractSQLiteHelper implements UserDao {

    @Inject
    public SqliteUserDao(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public User insertUser(@NotNull User user) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertUser(user), new InsertUserProperties(user)));
        return user;
    }

    @Nullable
    @Override
    public User loadUserById(@NotNull String userId) {
        final List<User> users = AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadByUserId(getContext(), userId, getSqliteOpenHelper(), this));
        return Collections.getFirstListElement(users);
    }

    @NotNull
    @Override
    public List<AProperty> loadUserPropertiesById(@NotNull String userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserPropertiesDbQuery(userId, getContext(), getSqliteOpenHelper()));
    }

    @Override
    public void updateUser(@NotNull User user) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateUser(user), new DeleteUserProperties(user), new InsertUserProperties(user)));
    }

    @NotNull
    @Override
    public List<String> loadUserIds() {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserIds(getContext(), getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<String> loadUserContactIds(@NotNull String userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadContactIdsByUserId(getContext(), userId, getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<User> loadUserContacts(@NotNull String userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadContactsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
    }

    @NotNull
    @Override
    public MergeDaoResult<User, String> mergeUserContacts(@NotNull String userId, @NotNull List<User> contacts) {
        final MergeDaoResultImpl<User, String> result = new MergeDaoResultImpl<User, String>(contacts);

        final List<String> contactIdsFromDb = loadUserContactIds(userId);
        for (final String contactIdFromDb : contactIdsFromDb) {
            try {
                // contact exists both in db and on remote server => just update contact properties
                result.addUpdatedObject(Iterables.find(contacts, new UserByIdFinder(contactIdFromDb)));
            } catch (NoSuchElementException e) {
                // contact was removed on remote server => need to remove from local db
                result.addRemovedObjectId(contactIdFromDb);
            }
        }

        final List<String> userIdsFromDb = loadUserIds();
        for (User contact : contacts) {
            try {
                // contact exists both in db and on remote server => case already covered above
                Iterables.find(contactIdsFromDb, Predicates.equalTo(contact.getRealmUser().getEntityId()));
            } catch (NoSuchElementException e) {
                // contact was added on remote server => need to add to local db
                if (userIdsFromDb.contains(contact.getRealmUser().getEntityId())) {
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
            execs.add(new InsertContact(userId, addedContactLink.getRealmUser().getEntityId()));
        }


        for (User addedContact : result.getAddedObjects()) {
            execs.add(new InsertUser(addedContact));
            execs.add(new InsertUserProperties(addedContact));
            execs.add(new InsertContact(userId, addedContact.getRealmUser().getEntityId()));
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

        @NotNull
        private String userId;

        @NotNull
        private String contactId;

        private InsertContact(@NotNull String userId, @NotNull String contactId) {
            this.userId = userId;
            this.contactId = contactId;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("contact_id", contactId);
            db.insert("user_contacts", null, values);
        }
    }

    private static final class RemoveContacts implements DbExec {

        @NotNull
        private String userId;

        @NotNull
        private List<String> contactIds;

        private RemoveContacts(@NotNull String userId, @NotNull List<String> contactIds) {
            this.userId = userId;
            this.contactIds = contactIds;
        }

        @NotNull
        private static List<RemoveContacts> newInstances(@NotNull String userId, @NotNull List<String> contactIds) {
            final List<RemoveContacts> result = new ArrayList<RemoveContacts>();

            for (List<String> contactIdsChunk : Collections.split(contactIds, MAX_IN_COUNT)) {
                result.add(new RemoveContacts(userId, contactIdsChunk));
            }

            return result;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            db.delete("user_contacts", "user_id = ? and contact_id in " + AndroidDbUtils.inClause(contactIds), AndroidDbUtils.inClauseValues(contactIds, userId));
        }
    }

    private static final class LoadContactIdsByUserId extends AbstractDbQuery<List<String>> {

        @NotNull
        private final String userId;

        private LoadContactIdsByUserId(@NotNull Context context, @NotNull String userId, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id in (select contact_id from user_contacts where user_id = ? ) ", new String[]{userId}, null, null, null);
        }

        @NotNull
        @Override
        public List<String> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
        }
    }

    private static final class LoadUserIds extends AbstractDbQuery<List<String>> {

        private LoadUserIds(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", new String[]{"id"}, null, null, null, null, null);
        }

        @NotNull
        @Override
        public List<String> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
        }
    }

    private static final class LoadContactsByUserId extends AbstractDbQuery<List<User>> {

        @NotNull
        private final String userId;

        @NotNull
        private final UserDao userDao;

        private LoadContactsByUserId(@NotNull Context context, @NotNull String userId, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull UserDao userDao) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
            this.userDao = userDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id in (select contact_id from user_contacts where user_id = ? ) ", new String[]{userId}, null, null, null);
        }

        @NotNull
        @Override
        public List<User> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<User>(new UserMapper(userDao)).convert(cursor);
        }
    }

    private static final class LoadByUserId extends AbstractDbQuery<List<User>> {

        @NotNull
        private final String userId;

        @NotNull
        private final UserDao userDao;

        private LoadByUserId(@NotNull Context context,
                             @NotNull String userId,
                             @NotNull SQLiteOpenHelper sqliteOpenHelper,
                             @NotNull UserDao userDao) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
            this.userDao = userDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id = ? ", new String[]{userId}, null, null, null);
        }

        @NotNull
        @Override
        public List<User> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<User>(new UserMapper(userDao)).convert(cursor);
        }
    }

    public static final class LoadUserPropertiesDbQuery extends PropertyByIdDbQuery {

        public LoadUserPropertiesDbQuery(@NotNull String userId, @NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper, "user_properties", "user_id", userId);
        }
    }

    private static final class InsertUser extends AbstractObjectDbExec<User> {

        private InsertUser(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            final ContentValues values = toContentValues(user);

            db.insert("users", null, values);
        }
    }


    private static final class UpdateUser extends AbstractObjectDbExec<User> {

        private UpdateUser(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            final ContentValues values = toContentValues(user);

            db.update("users", values, "id = ?", new String[]{String.valueOf(user.getRealmUser().getEntityId())});
        }
    }

    private static final class DeleteUserProperties extends AbstractObjectDbExec<User> {

        private DeleteUserProperties(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            db.delete("user_properties", "user_id = ?", new String[]{String.valueOf(user.getRealmUser().getEntityId())});
        }
    }

    private static final class InsertUserProperties extends AbstractObjectDbExec<User> {

        private InsertUserProperties(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            for (AProperty property : user.getProperties()) {
                final ContentValues values = new ContentValues();
                final String value = property.getValue();
                if (value != null) {
                    values.put("user_id", user.getRealmUser().getEntityId());
                    values.put("property_name", property.getName());
                    values.put("property_value", value);
                    db.insert("user_properties", null, values);
                }
            }
        }
    }

    private static class UserByIdFinder implements Predicate<User> {

        @NotNull
        private final String userId;

        public UserByIdFinder(@NotNull String userId) {
            this.userId = userId;
        }

        @Override
        public boolean apply(@javax.annotation.Nullable User user) {
            return user != null && userId.equals(user.getRealmUser().getEntityId());
        }
    }

    @NotNull
    private static ContentValues toContentValues(@NotNull User user) {
        final ContentValues values = new ContentValues();

        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

        final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
        final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();

        values.put("id", user.getRealmUser().getEntityId());
        values.put("realm_id", user.getRealmUser().getRealmId());
        values.put("realm_user_id", user.getRealmUser().getRealmEntityId());
        values.put("last_properties_sync_date", lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(lastPropertiesSyncDate));
        values.put("last_contacts_sync_date", lastContactsSyncDate == null ? null : dateTimeFormatter.print(lastContactsSyncDate));

        return values;
    }
}
