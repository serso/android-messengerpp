package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import javax.annotation.Nonnull;
import org.solovyev.common.Converter;

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
        final String realmId = cursor.getString(cursorPosition);
        final String realmEntityId = cursor.getString(cursorPosition + 1);

        return RealmEntityImpl.newInstance(realmId, realmEntityId);
    }
}
