/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.db.DbExec;
import org.solovyev.android.messenger.entities.Entities;

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
			return db.delete(tableName, foreignKeyColumnName + " like '" + accountId + Entities.DELIMITER + "%'", null);
		}

		return 0;
	}
}
