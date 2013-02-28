package org.solovyev.android.db;

import android.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;

public class DeleteAllRowsDbExec implements DbExec {

    @NotNull
    private String tableName;

    private DeleteAllRowsDbExec(@NotNull String tableName) {
        this.tableName = tableName;
    }

    @NotNull
    public static DbExec newInstance(@NotNull String tableName) {
        return new DeleteAllRowsDbExec(tableName);
    }

    @Override
    public void exec(@NotNull SQLiteDatabase db) {
        db.delete(tableName, null, null);
    }
}
