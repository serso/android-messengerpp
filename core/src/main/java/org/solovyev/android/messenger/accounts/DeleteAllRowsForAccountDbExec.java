package org.solovyev.android.messenger.accounts;

import android.database.sqlite.SQLiteDatabase;
import org.solovyev.android.db.DbExec;
import org.solovyev.android.messenger.entities.EntityImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 9:02 PM
 */
public class DeleteAllRowsForAccountDbExec implements DbExec {

	@Nonnull
	private final String tableName;

	@Nullable
	private final String realmColumnName;

	@Nullable
	private final String foreignKeyColumnName;

	@Nonnull
	private final String accountId;

	private DeleteAllRowsForAccountDbExec(@Nonnull String tableName,
										  @Nullable String realmColumnName,
										  @Nullable String foreignKeyColumnName,
										  @Nonnull String accountId) {
		this.tableName = tableName;
		this.realmColumnName = realmColumnName;
		this.foreignKeyColumnName = foreignKeyColumnName;
		this.accountId = accountId;
	}

	@Nonnull
	public static DeleteAllRowsForAccountDbExec newInstance(@Nonnull String tableName, @Nonnull String realmColumnName, @Nonnull String realmId) {
		return new DeleteAllRowsForAccountDbExec(tableName, realmColumnName, null, realmId);
	}

	@Nonnull
	public static DeleteAllRowsForAccountDbExec newStartsWith(@Nonnull String tableName, @Nonnull String foreignKeyColumnName, @Nonnull String realmId) {
		return new DeleteAllRowsForAccountDbExec(tableName, null, foreignKeyColumnName, realmId);
	}


	@Override
	public long exec(@Nonnull SQLiteDatabase db) {
		if (realmColumnName != null) {
			return db.delete(tableName, realmColumnName + " = ?", new String[]{accountId});
		} else if (foreignKeyColumnName != null) {
			// todo serso: use ?
			return db.delete(tableName, foreignKeyColumnName + " like '" + accountId + EntityImpl.DELIMITER + "%'", null);
		}

		return 0;
	}
}
