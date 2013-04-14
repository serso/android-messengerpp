package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.solovyev.android.db.*;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Singleton
public class SqliteRealmDao extends AbstractSQLiteHelper implements RealmDao {

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Nullable
    private SecretKey secret;

    @Inject
    public SqliteRealmDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    SqliteRealmDao(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @Override
    public void init() {
        secret = MessengerApplication.getServiceLocator().getSecurityService().getSecretKey();
    }

    @Override
    public void insertRealm(@Nonnull Realm realm) throws RealmException {
        try {
            AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertRealm(realm, secret)));
        } catch (RealmRuntimeException e) {
            throw new RealmException(e);
        }
    }

    @Override
    public void deleteRealm(@Nonnull String realmId) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteRealm(realmId)));
    }

    @Nonnull
    @Override
    public Collection<Realm> loadRealms() {
        try {
            return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), null, getSqliteOpenHelper()));
        } catch (RealmRuntimeException e) {
            MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteAllRealms() {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance("realms")));
    }

    @Override
    public void updateRealm(@Nonnull Realm realm) throws RealmException {
        try {
            AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateRealm(realm, secret)));
        } catch (RealmRuntimeException e) {
            throw new RealmException(e);
        }
    }

    @Nonnull
    @Override
    public Collection<Realm> loadRealmsInState(@Nonnull RealmState state) {
        try {
            return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), state, getSqliteOpenHelper()));
        } catch (RealmRuntimeException e) {
            MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
            return Collections.emptyList();
        }
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static class InsertRealm extends AbstractObjectDbExec<Realm> {

        @Nullable
        private final SecretKey secret;

        public InsertRealm(@Nonnull Realm realm, @Nullable SecretKey secret) {
            super(realm);
            this.secret = secret;
        }

        @Override
        public long exec(@Nonnull SQLiteDatabase db) {
            final Realm realm = getNotNullObject();

            final ContentValues values = toContentValues(realm, secret);

            return db.insert("realms", null, values);
        }
    }

    private static class UpdateRealm extends AbstractObjectDbExec<Realm> {

        @Nullable
        private final SecretKey secret;

        public UpdateRealm(@Nonnull Realm realm, @Nullable SecretKey secret) {
            super(realm);
            this.secret = secret;
        }

        @Override
        public long exec(@Nonnull SQLiteDatabase db) {
            final Realm realm = getNotNullObject();

            final ContentValues values = toContentValues(realm, secret);

            return db.update("realms", values, "id = ?", new String[]{realm.getId()});
        }
    }

    @Nonnull
    private static ContentValues toContentValues(@Nonnull Realm realm, @Nullable SecretKey secret) throws RealmRuntimeException {
        final ContentValues values = new ContentValues();

        values.put("id", realm.getId());
        values.put("realm_def_id", realm.getRealmDef().getId());
        values.put("user_id", realm.getUser().getEntity().getEntityId());

        final RealmConfiguration configuration;

        try {
            final Cipherer<RealmConfiguration, RealmConfiguration> cipherer = realm.getRealmDef().getCipherer();
            if (cipherer != null && secret != null) {
                configuration = cipherer.encrypt(secret, realm.getConfiguration());
            } else {
                configuration = realm.getConfiguration();
            }
            values.put("configuration", new Gson().toJson(configuration));
        } catch (CiphererException e) {
            throw new RealmRuntimeException(e);
        }

        values.put("state", realm.getState().name());

        return values;
    }

    private class LoadRealm extends AbstractDbQuery<Collection<Realm>> {

        @Nullable
        private final RealmState state;

        protected LoadRealm(@Nonnull Context context, @Nullable RealmState state, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.state = state;
        }

        @Nonnull
        @Override
        public Cursor createCursor(@Nonnull SQLiteDatabase db) {
            if (state == null) {
                return db.query("realms", null, null, null, null, null, null);
            } else {
                return db.query("realms", null, "state = ?", new String[]{state.name()}, null, null, null);
            }
        }

        @Nonnull
        @Override
        public Collection<Realm> retrieveData(@Nonnull Cursor cursor) {
            return new ListMapper<Realm>(new RealmMapper(secret)).convert(cursor);
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
