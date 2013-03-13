package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.db.AbstractSQLiteHelper;
import org.solovyev.android.db.AndroidDbUtils;
import org.solovyev.android.db.DbExec;
import org.solovyev.android.db.DeleteAllRowsDbExec;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

@Singleton
public class SqliteRealmDao extends AbstractSQLiteHelper implements RealmDao {

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    public SqliteRealmDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    SqliteRealmDao(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @Override
    public void insertRealm(@Nonnull Realm realm) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertRealm(realm)));
    }

    @Override
    public void deleteRealm(@Nonnull String realmId) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteRealm(realmId)));
    }

    @Nonnull
    @Override
    public Collection<Realm> loadRealms() {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), getSqliteOpenHelper()));
    }

    @Override
    public void deleteAllRealms() {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance("realms")));
    }

    @Override
    public void updateRealm(@Nonnull Realm realm) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateRealm(realm)));
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static class InsertRealm extends AbstractObjectDbExec<Realm> {

        public InsertRealm(@Nonnull Realm realm) {
            super(realm);
        }

        @Override
        public long exec(@Nonnull SQLiteDatabase db) {
            final Realm realm = getNotNullObject();

            final ContentValues values = toContentValues(realm);

            return db.insert("realms", null, values);
        }
    }

    private static class UpdateRealm extends AbstractObjectDbExec<Realm> {

        public UpdateRealm(@Nonnull Realm realm) {
            super(realm);
        }

        @Override
        public long exec(@Nonnull SQLiteDatabase db) {
            final Realm realm = getNotNullObject();

            final ContentValues values = toContentValues(realm);

            return db.update("realms", values, "id = ?", new String[]{realm.getId()});
        }
    }

    @Nonnull
    private static ContentValues toContentValues(@Nonnull Realm realm) {
        final ContentValues values = new ContentValues();

        values.put("id", realm.getId());
        values.put("realm_def_id", realm.getRealmDef().getId());
        values.put("user_id", realm.getUser().getEntity().getEntityId());
        values.put("configuration", new Gson().toJson(realm.getConfiguration()));

        return values;
    }

    private class LoadRealm extends AbstractDbQuery<Collection<Realm>> {

        protected LoadRealm(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
        }

        @Nonnull
        @Override
        public Cursor createCursor(@Nonnull SQLiteDatabase db) {
            return db.query("realms", null, null, null, null, null, null);
        }

        @Nonnull
        @Override
        public Collection<Realm> retrieveData(@Nonnull Cursor cursor) {
            return new ListMapper<Realm>(new RealmMapper(realmService, userService)).convert(cursor);
        }
    }

    private static class DeleteRealm extends AbstractObjectDbExec<String> {

        public DeleteRealm(@Nonnull String realmId) {
            super(realmId);
        }

        @Override
        public long exec(@Nonnull SQLiteDatabase db) {
            final String realmId = getNotNullObject();

            return db.delete("realms", "id = ?", new String[]{realmId});
        }
    }

}
