package org.solovyev.android.messenger.db;

import android.database.Cursor;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:08 PM
 */
public class IdMapper implements Converter<Cursor, Integer> {

	@Nonnull
	private static final IdMapper instance = new IdMapper();

	private IdMapper() {
	}

	@Nonnull
	public static IdMapper getInstance() {
		return instance;
	}

	@Nonnull
	@Override
	public Integer convert(@Nonnull Cursor cursor) {
		return cursor.getInt(0);
	}
}
