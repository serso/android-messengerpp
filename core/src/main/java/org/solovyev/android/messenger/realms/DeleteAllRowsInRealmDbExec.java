package org.solovyev.android.messenger.realms;

import android.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.db.DbExec;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 9:02 PM
 */
public class DeleteAllRowsInRealmDbExec implements DbExec {

    @NotNull
    private final String tableName;

    @Nullable
    private final String realmColumnName;

    @Nullable
    private final String foreignKeyColumnName;

    @NotNull
    private final String realmId;

    private DeleteAllRowsInRealmDbExec(@NotNull String tableName,
                                       @Nullable String realmColumnName,
                                       @Nullable String foreignKeyColumnName,
                                       @NotNull String realmId) {
        this.tableName = tableName;
        this.realmColumnName = realmColumnName;
        this.foreignKeyColumnName = foreignKeyColumnName;
        this.realmId = realmId;
    }

    @NotNull
    public static DeleteAllRowsInRealmDbExec newInstance(@NotNull String tableName, @NotNull String realmColumnName, @NotNull String realmId) {
        return new DeleteAllRowsInRealmDbExec(tableName, realmColumnName, null, realmId);
    }

    @NotNull
    public static DeleteAllRowsInRealmDbExec newStartsWith(@NotNull String tableName, @NotNull String foreignKeyColumnName, @NotNull String realmId) {
        return new DeleteAllRowsInRealmDbExec(tableName, null, foreignKeyColumnName, realmId);
    }


    @Override
    public void exec(@NotNull SQLiteDatabase db) {
        if (realmColumnName != null) {
            db.delete(tableName, realmColumnName + " = ?", new String[]{realmId});
        } else if (foreignKeyColumnName != null) {
            // todo serso: use ?
            db.delete(tableName, foreignKeyColumnName + " like '" + realmId + RealmEntityImpl.DELIMITER + "%'", null);
        }
    }
}
