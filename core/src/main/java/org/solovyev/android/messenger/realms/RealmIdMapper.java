package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 2:00 AM
 */
public class RealmIdMapper<I> implements Converter<Cursor, RealmEntity> {

    private int cursorPosition;

    private RealmIdMapper(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    @NotNull
    public static <I> RealmIdMapper<I> newInstanceFor(int cursorPosition) {
        return new RealmIdMapper<I>(cursorPosition);
    }

    @NotNull
    @Override
    public final RealmEntity convert(@NotNull Cursor cursor) {
        final String realmId = cursor.getString(cursorPosition);
        final String realmEntityId = cursor.getString(cursorPosition + 1);

        return RealmEntityImpl.newInstance(realmId, realmEntityId);
    }
}
