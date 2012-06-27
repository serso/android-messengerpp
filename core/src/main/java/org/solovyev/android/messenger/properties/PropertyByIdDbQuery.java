package org.solovyev.android.messenger.properties;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.AProperty;
import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.ListMapper;

import java.util.List;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 7:24 PM
 */
public class PropertyByIdDbQuery extends AbstractDbQuery<List<AProperty>> {

    @NotNull
    private String tableName;

    @NotNull
    private String idColumnName;

    @NotNull
    private String id;

    public PropertyByIdDbQuery(@NotNull Context context,
                               @NotNull SQLiteOpenHelper sqliteOpenHelper,
                               @NotNull String tableName,
                               @NotNull String idColumnName,
                               @NotNull String id) {
        super(context, sqliteOpenHelper);
        this.tableName = tableName;
        this.idColumnName = idColumnName;
        this.id = id;
    }

    @NotNull
    @Override
    public Cursor createCursor(@NotNull SQLiteDatabase db) {
        return db.query(tableName, null, idColumnName + " = ? ", new String[]{String.valueOf(id)}, null, null, null);
    }

    @NotNull
    @Override
    public List<AProperty> retrieveData(@NotNull Cursor cursor) {
        return new ListMapper<AProperty>(APropertyMapper.getInstance()).convert(cursor);
    }
}
