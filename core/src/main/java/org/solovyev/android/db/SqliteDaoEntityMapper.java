package org.solovyev.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

public interface SqliteDaoEntityMapper<E extends Identifiable> {

	@Nonnull
	ContentValues toContentValues(@Nonnull E entity);

	@Nonnull
	Converter<Cursor, E> getCursorMapper();
}
