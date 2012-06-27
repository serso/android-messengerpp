package org.solovyev.android.messenger.db;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.common.utils.Converter;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:08 PM
 */
public class IdMapper implements Converter<Cursor, Integer> {

    @NotNull
    private static final IdMapper instance = new IdMapper();

    private IdMapper() {
    }

    @NotNull
    public static IdMapper getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public Integer convert(@NotNull Cursor cursor) {
        return cursor.getInt(0);
    }
}
