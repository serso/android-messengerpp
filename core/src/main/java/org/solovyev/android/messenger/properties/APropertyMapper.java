package org.solovyev.android.messenger.properties;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.AProperty;
import org.solovyev.android.APropertyImpl;
import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 7:26 PM
 */
public class APropertyMapper implements Converter<Cursor, AProperty> {

    @NotNull
    private static final APropertyMapper instance = new APropertyMapper();

    private APropertyMapper() {
    }

    @NotNull
    public static APropertyMapper getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public AProperty convert(@NotNull Cursor cursor) {
        final String id = cursor.getString(0);
        final String name = cursor.getString(1);
        final String value = cursor.getString(2);
        return APropertyImpl.newInstance(name, value);
    }
}
