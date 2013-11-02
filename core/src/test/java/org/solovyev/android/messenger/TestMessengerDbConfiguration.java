package org.solovyev.android.messenger;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

public class TestMessengerDbConfiguration implements SQLiteOpenHelperConfiguration {
	@Nonnull
	@Override
	public String getName() {
		return "test";
	}

	@Nullable
	@Override
	public SQLiteDatabase.CursorFactory getCursorFactory() {
		return null;
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
