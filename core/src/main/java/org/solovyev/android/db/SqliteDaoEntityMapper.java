package org.solovyev.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

public interface SqliteDaoEntityMapper<E> {

	@Nonnull
	ContentValues toContentValues(@Nonnull E entity);

	@Nonnull
	Converter<Cursor, E> getCursorMapper();

	@Nonnull
	String getId(@Nonnull E entity);
}
