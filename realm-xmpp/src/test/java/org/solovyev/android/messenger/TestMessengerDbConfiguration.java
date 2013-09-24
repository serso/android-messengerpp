package org.solovyev.android.messenger;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 8:49 PM
 */
public class TestMessengerDbConfiguration implements SQLiteOpenHelperConfiguration {
	@Nonnull
	@Override
	public String getName() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Nullable
	@Override
	public SQLiteDatabase.CursorFactory getCursorFactory() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getVersion() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
