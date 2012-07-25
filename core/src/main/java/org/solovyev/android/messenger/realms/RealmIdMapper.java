package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.common.utils.Converter;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 2:00 AM
 */
public abstract class RealmIdMapper<I> implements Converter<Cursor, RealmId<I>> {

    private int cursorPosition;

    protected RealmIdMapper(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    @NotNull
    @Override
    public final RealmId<I> convert(@NotNull Cursor cursor) {
        final String realm = cursor.getString(cursorPosition);
        final I id = getId(cursor, cursorPosition + 1);

        return new RealmIdImpl<I>(realm, id);
    }

    @NotNull
    protected abstract I getId(@NotNull Cursor cursor, int cursorPosition);

    private static class StringRealmIdMapper extends RealmIdMapper<String> {

        protected StringRealmIdMapper(int cursorPosition) {
            super(cursorPosition);
        }

        @NotNull
        @Override
        protected String getId(@NotNull Cursor cursor, int cursorPosition) {
            return cursor.getString(cursorPosition);
        }
    }

    @NotNull
    public static Converter<Cursor, RealmId<String>> forStringId(int cursorPosition) {
        return new StringRealmIdMapper(cursorPosition);
    }
}
