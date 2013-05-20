package org.solovyev.android.messenger.db;

import android.database.Cursor;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:17 PM
 */
public class StringIdMapper implements Converter<Cursor, String> {

	@Nonnull
	private static final StringIdMapper instance = new StringIdMapper();

	private StringIdMapper() {
	}

	@Nonnull
	public static StringIdMapper getInstance() {
		return instance;
	}

	@Nonnull
	@Override
	public String convert(@Nonnull Cursor cursor) {
		return cursor.getString(0);
	}
}
