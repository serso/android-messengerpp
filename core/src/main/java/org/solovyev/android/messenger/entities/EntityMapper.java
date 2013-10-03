package org.solovyev.android.messenger.entities;

import android.database.Cursor;

import javax.annotation.Nonnull;

import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 2:00 AM
 */
public class EntityMapper implements Converter<Cursor, Entity> {

	private int cursorPosition;

	private EntityMapper(int cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	@Nonnull
	public static EntityMapper newInstanceFor(int cursorPosition) {
		return new EntityMapper(cursorPosition);
	}

	@Nonnull
	@Override
	public final Entity convert(@Nonnull Cursor cursor) {
		final String entityId = cursor.getString(cursorPosition);
		final String accountId = cursor.getString(cursorPosition + 1);
		final String accountEntityId = cursor.getString(cursorPosition + 2);

		return Entities.newEntity(accountId, accountEntityId, entityId);
	}
}
