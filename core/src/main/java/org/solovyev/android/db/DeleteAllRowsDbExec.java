package org.solovyev.android.db;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

public class DeleteAllRowsDbExec implements DbExec {

	@Nonnull
	private String tableName;

	private DeleteAllRowsDbExec(@Nonnull String tableName) {
		this.tableName = tableName;
	}

	@Nonnull
	public static DbExec newInstance(@Nonnull String tableName) {
		return new DeleteAllRowsDbExec(tableName);
	}

	@Override
	public long exec(@Nonnull SQLiteDatabase db) {
		return db.delete(tableName, "1", null);
	}
}
