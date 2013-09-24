package org.solovyev.android.messenger;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 12:16 AM
 */
@Singleton
public class MessengerDbConfiguration implements SQLiteOpenHelperConfiguration {

	@Nonnull
	public static final String DB_NAME = "mpp";
	public static final int DB_VERSION = 1;

	@Nonnull
	@Override
	public String getName() {
		return DB_NAME;
	}

	@Override
	public SQLiteDatabase.CursorFactory getCursorFactory() {
		return null;
	}

	@Override
	public int getVersion() {
		return DB_VERSION;
	}
}
