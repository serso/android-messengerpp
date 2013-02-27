package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.db.AbstractSQLiteHelper;
import org.solovyev.android.db.AndroidDbUtils;
import org.solovyev.android.db.DbExec;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

@Singleton
public class SqliteRealmDao extends AbstractSQLiteHelper implements RealmDao {

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    public SqliteRealmDao(@NotNull Application context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    SqliteRealmDao(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public Realm insertRealm(@NotNull Realm realm) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertRealm(realm)));
        return realm;
    }

    @NotNull
    @Override
    public Collection<Realm> loadRealms() {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), getSqliteOpenHelper()));
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static class InsertRealm extends AbstractObjectDbExec<Realm> {

        public InsertRealm(@NotNull Realm realm) {
            super(realm);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final Realm realm = getNotNullObject();

            final ContentValues values = toContentValues(realm);

            db.insert("realms", null, values);
        }
    }

    @NotNull
    private static ContentValues toContentValues(@NotNull Realm realm) {
        final ContentValues values = new ContentValues();

        values.put("id", realm.getId());
        values.put("realm_def_id", realm.getRealmDef().getId());
        values.put("user_id", realm.getUser().getRealmUser().getEntityId());
        values.put("configuration", new Gson().toJson(realm.getConfiguration()));

        return values;
    }

    private class LoadRealm extends AbstractDbQuery<Collection<Realm>> {

        protected LoadRealm(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("realms", null, null, null, null, null, null);
        }

        @NotNull
        @Override
        public Collection<Realm> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<Realm>(new RealmMapper(realmService, userService)).convert(cursor);
        }
    }
}
