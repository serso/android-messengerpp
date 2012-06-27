package org.solovyev.android.messenger.db;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.common.utils.Converter;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:17 PM
 */
public class StringIdMapper implements Converter<Cursor, String> {

    @NotNull
    private static final StringIdMapper instance = new StringIdMapper();

    private StringIdMapper() {
    }

    @NotNull
    public static StringIdMapper getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public String convert(@NotNull Cursor cursor) {
        return cursor.getString(0);
    }
}
