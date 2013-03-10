package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 2:00 AM
 */
public class RealmEntityMapper implements Converter<Cursor, RealmEntity> {

    private int cursorPosition;

    private RealmEntityMapper(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    @Nonnull
    public static RealmEntityMapper newInstanceFor(int cursorPosition) {
        return new RealmEntityMapper(cursorPosition);
    }

    @Nonnull
    @Override
    public final RealmEntity convert(@Nonnull Cursor cursor) {
        final String entityId = cursor.getString(cursorPosition);
        final String realmId = cursor.getString(cursorPosition + 1);
        final String realmEntityId = cursor.getString(cursorPosition + 2);

        return RealmEntityImpl.newInstance(realmId, realmEntityId, entityId);
    }
}
