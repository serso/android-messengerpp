package org.solovyev.android.messenger.db;

import android.app.Application;
import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.db.CommonSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 11:45 PM
 */
@Singleton
public class MessengerSQLiteOpenHelper extends CommonSQLiteOpenHelper {

	@Inject
	public MessengerSQLiteOpenHelper(@Nonnull Application context,
									 @Nonnull SQLiteOpenHelperConfiguration configuration) {
		super(context, configuration);
	}

	public MessengerSQLiteOpenHelper(@Nonnull Context context,
									 @Nonnull SQLiteOpenHelperConfiguration configuration) {
		super(context, configuration);
	}
}
