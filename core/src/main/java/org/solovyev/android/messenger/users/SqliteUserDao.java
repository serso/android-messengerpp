package org.solovyev.android.messenger.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.AProperty;
import org.solovyev.android.db.*;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.db.IdMapper;
import org.solovyev.android.messenger.properties.PropertyByIdDbQuery;
import org.solovyev.common.utils.CollectionsUtils;
import org.solovyev.common.utils.CollectionsUtils2;

import java.util.*;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 2:13 AM
 */
public class SqliteUserDao extends AbstractSQLiteHelper implements UserDao {

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
    public User loadUserById(@NotNull Integer userId) {
        final List<User> users = AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadByUserId(getContext(), userId, getSqliteOpenHelper(), this));
        return CollectionsUtils.getFirstListElement(users);
    }

    @NotNull
    @Override
    public List<AProperty> loadUserPropertiesById(@NotNull Integer userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserPropertiesDbQuery(userId, getContext(), getSqliteOpenHelper()));
    }

    @Override
    public void updateUser(@NotNull User user) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateUser(user), new DeleteUserProperties(user), new InsertUserProperties(user)));
    }

    @NotNull
    @Override
    public List<Integer> loadUserIds() {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadUserIds(getContext(), getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<Integer> loadUserFriendIds(@NotNull Integer userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadFriendIdsByUserId(getContext(), userId, getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<User> loadUserFriends(@NotNull Integer userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadFriendsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
    }

    @NotNull
    @Override
    public MergeDaoResult<User, Integer> mergeUserFriends(@NotNull Integer userId, @NotNull List<User> friends) {
        final MergeDaoResultImpl<User, Integer> result = new MergeDaoResultImpl<User, Integer>(friends);

        final List<Integer> friendIdsFromDb = loadUserFriendIds(userId);
        for (final Integer friendIdFromDb : friendIdsFromDb) {
            try {
                // friend exists both in db and on remote server => just update friend properties
                result.addUpdatedObject(Iterables.find(friends, new UserByIdFinder(friendIdFromDb)));
            } catch (NoSuchElementException e) {
                // friend was removed on remote server => need to remove from local db
                result.addRemovedObjectId(friendIdFromDb);
            }
        }

        final List<Integer> userIdsFromDb = loadUserIds();
        for (User friend : friends) {
            try {
                // friend exists both in db and on remote server => case already covered above
                Iterables.find(friendIdsFromDb, Predicates.equalTo(friend.getId()));
            } catch (NoSuchElementException e) {
                // friend was added on remote server => need to add to local db
                if (userIdsFromDb.contains(friend.getId())) {
                    // only link must be added - user already in users table
                    result.addAddedObjectLink(friend);
                } else {
                    // no user information in local db is available - full user insertion
                    result.addAddedObject(friend);
                }
            }
        }

        final List<DbExec> execs = new ArrayList<DbExec>();

        if (!result.getRemovedObjectIds().isEmpty()) {
            execs.addAll(RemoveFriends.newInstances(userId, result.getRemovedObjectIds()));
        }

        for (User updatedFriend : result.getUpdatedObjects()) {
            execs.add(new UpdateUser(updatedFriend));
            execs.add(new DeleteUserProperties(updatedFriend));
            execs.add(new InsertUserProperties(updatedFriend));
        }

        for (User addedFriendLink : result.getAddedObjectLinks()) {
            execs.add(new UpdateUser(addedFriendLink));
            execs.add(new DeleteUserProperties(addedFriendLink));
            execs.add(new InsertUserProperties(addedFriendLink));
            execs.add(new InsertFriend(userId, addedFriendLink.getId()));
        }


        for (User addedFriend : result.getAddedObjects()) {
            execs.add(new InsertUser(addedFriend));
            execs.add(new InsertUserProperties(addedFriend));
            execs.add(new InsertFriend(userId, addedFriend.getId()));
        }

        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), execs);

        return result;
    }


    private static final class InsertFriend implements DbExec {

        @NotNull
        private Integer userId;

        @NotNull
        private Integer friendId;

        private InsertFriend(@NotNull Integer userId, @NotNull Integer friendId) {
            this.userId = userId;
            this.friendId = friendId;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("friend_id", friendId);
            db.insert("user_friends", null, values);
        }
    }

    private static final class RemoveFriends implements DbExec {

        @NotNull
        private Integer userId;

        @NotNull
        private List<Integer> friendIds;

        private RemoveFriends(@NotNull Integer userId, @NotNull List<Integer> friendIds) {
            this.userId = userId;
            this.friendIds = friendIds;
        }

        @NotNull
        private static List<RemoveFriends> newInstances(@NotNull Integer userId, @NotNull List<Integer> friendIds) {
            final List<RemoveFriends> result = new ArrayList<RemoveFriends>();

            for (List<Integer> friendIdsChunk : CollectionsUtils2.split(friendIds, MAX_IN_COUNT)) {
                result.add(new RemoveFriends(userId, friendIdsChunk));
            }

            return result;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            db.delete("user_friends", "user_id = ? and friend_id in " + AndroidDbUtils.inClause(friendIds), AndroidDbUtils.inClauseValues(friendIds, String.valueOf(userId)));
        }
    }

    private static final class LoadFriendIdsByUserId extends AbstractDbQuery<List<Integer>> {

        @NotNull
        private final Integer userId;

        private LoadFriendIdsByUserId(@NotNull Context context, @NotNull Integer userId, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id in (select friend_id from user_friends where user_id = ? ) ", new String[]{String.valueOf(userId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<Integer> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<Integer>(IdMapper.getInstance()).convert(cursor);
        }
    }

    private static final class LoadUserIds extends AbstractDbQuery<List<Integer>> {

        private LoadUserIds(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, null, null, null, null, null);
        }

        @NotNull
        @Override
        public List<Integer> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<Integer>(IdMapper.getInstance()).convert(cursor);
        }
    }

    private static final class LoadFriendsByUserId extends AbstractDbQuery<List<User>> {

        @NotNull
        private final Integer userId;

        @NotNull
        private final UserDao userDao;

        private LoadFriendsByUserId(@NotNull Context context, @NotNull Integer userId, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull UserDao userDao) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
            this.userDao = userDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id in (select friend_id from user_friends where user_id = ? ) ", new String[]{String.valueOf(userId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<User> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<User>(new UserMapper(userDao)).convert(cursor);
        }
    }

    private static final class LoadByUserId extends AbstractDbQuery<List<User>> {

        @NotNull
        private final Integer userId;

        @NotNull
        private final UserDao userDao;

        private LoadByUserId(@NotNull Context context, @NotNull Integer userId, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull UserDao userDao) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
            this.userDao = userDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, "id = ? ", new String[]{String.valueOf(userId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<User> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<User>(new UserMapper(userDao)).convert(cursor);
        }
    }

    public static final class LoadUserPropertiesDbQuery extends PropertyByIdDbQuery {

        public LoadUserPropertiesDbQuery(@NotNull Integer userId, @NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper, "user_properties", "user_id", String.valueOf(userId));
        }
    }

    private static final class InsertUser extends AbstractObjectDbExec<User> {

        private InsertUser(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
            final DateTime lastFriendsSyncDate = user.getUserSyncData().getLastFriendsSyncDate();

            final ContentValues values = new ContentValues();

            values.put("id", user.getId());
            values.put("version", user.getVersion());
            values.put("last_properties_sync_date", lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(lastPropertiesSyncDate));
            values.put("last_friends_sync_date", lastFriendsSyncDate == null ? null : dateTimeFormatter.print(lastFriendsSyncDate));

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

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
            final DateTime lastFriendsSyncDate = user.getUserSyncData().getLastFriendsSyncDate();

            final ContentValues values = new ContentValues();
            values.put("id", user.getId());
            values.put("version", user.getVersion());
            values.put("last_properties_sync_date", lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(lastPropertiesSyncDate));
            values.put("last_friends_sync_date", lastFriendsSyncDate == null ? null : dateTimeFormatter.print(lastFriendsSyncDate));

            db.update("users", values, "id = ?", new String[]{String.valueOf(user.getId())});
        }
    }

    private static final class DeleteUserProperties extends AbstractObjectDbExec<User> {

        private DeleteUserProperties(@NotNull User user) {
            super(user);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final User user = getNotNullObject();

            db.delete("user_properties", "user_id = ?", new String[]{String.valueOf(user.getId())});
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
                values.put("user_id", user.getId());
                values.put("property_name", property.getName());
                values.put("property_value", property.getValue());
                db.insert("user_properties", null, values);
            }
        }
    }

    private static class UserByIdFinder implements Predicate<User> {

        @NotNull
        private final Integer userId;

        public UserByIdFinder(@NotNull Integer userId) {
            this.userId = userId;
        }

        @Override
        public boolean apply(@javax.annotation.Nullable User user) {
            return user != null && userId.equals(user.getId());
        }
    }
}
