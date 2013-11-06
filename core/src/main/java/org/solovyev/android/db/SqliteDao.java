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

package org.solovyev.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.db.StringIdMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.getFirst;
import static org.solovyev.android.db.AndroidDbUtils.doDbExec;
import static org.solovyev.android.db.AndroidDbUtils.doDbExecs;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;

public final class SqliteDao<E extends Identifiable> extends AbstractSQLiteHelper implements Dao<E> {

	@Nonnull
	private final String tableName;

	@Nonnull
	private final String idColumnName;

	@Nonnull
	private final SqliteDaoEntityMapper<E> mapper;

	public SqliteDao(@Nonnull String tableName,
					 @Nonnull String idColumnName,
					 @Nonnull SqliteDaoEntityMapper<E> mapper,
					 @Nonnull Context context,
					 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.mapper = mapper;
	}

	@Override
	public long create(@Nonnull E entity) {
		return doDbExec(getSqliteOpenHelper(), new InsertEntity(entity));
	}

	@Nullable
	@Override
	public E read(@Nonnull String id) {
		final Collection<E> accounts = doDbQuery(getSqliteOpenHelper(), new LoadEntity(getContext(), id, getSqliteOpenHelper()));
		return getFirst(accounts, null);
	}

	@Nonnull
	@Override
	public Collection<E> readAll() {
		return doDbQuery(getSqliteOpenHelper(), new LoadEntity(getContext(), null, getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return doDbQuery(getSqliteOpenHelper(), new LoadIds(getContext(), getSqliteOpenHelper()));
	}

	@Override
	public long update(@Nonnull E entity) {
		return doDbExec(getSqliteOpenHelper(), new UpdateEntity(entity));
	}

	@Override
	public void delete(@Nonnull E entity) {
		deleteById(entity.getId());
	}

	@Override
	public void deleteById(@Nonnull String id) {
		doDbExec(getSqliteOpenHelper(), new DeleteEntity(id));
	}

	@Override
	public void deleteAll() {
		doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance(tableName)));
	}

	/*
	**********************************************************************
	*
	*                           STATIC
	*
	**********************************************************************
	*/

	private class InsertEntity extends AbstractObjectDbExec<E> {

		public InsertEntity(@Nonnull E entity) {
			super(entity);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final E entity = getNotNullObject();

			final ContentValues values = mapper.toContentValues(entity);

			return db.insert(tableName, null, values);
		}
	}

	private class UpdateEntity extends AbstractObjectDbExec<E> {

		public UpdateEntity(@Nonnull E entity) {
			super(entity);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final E entity = getNotNullObject();

			final ContentValues values = mapper.toContentValues(entity);

			return db.update(tableName, values, whereIdEqualsTo(), new String[]{entity.getId()});
		}
	}

	private class LoadEntity extends AbstractDbQuery<Collection<E>> {

		@Nullable
		private final String id;

		protected LoadEntity(@Nonnull Context context,
							 @Nullable String id,
							 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.id = id;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			if (id != null) {
				return db.query(tableName, null, whereIdEqualsTo(), new String[]{id}, null, null, null);
			} else {
				return db.query(tableName, null, null, null, null, null, null);
			}
		}

		@Nonnull
		@Override
		public Collection<E> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<E>(mapper.getCursorMapper()).convert(cursor);
		}
	}


	private class DeleteEntity extends AbstractObjectDbExec<String> {

		public DeleteEntity(@Nonnull String id) {
			super(id);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final String id = getNotNullObject();
			return db.delete(tableName, whereIdEqualsTo(), new String[]{id});
		}
	}

	@Nonnull
	private String whereIdEqualsTo() {
		return idColumnName + " = ?";
	}

	private final class LoadIds extends AbstractDbQuery<List<String>> {

		private LoadIds(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query(tableName, new String[]{idColumnName}, null, null, null, null, null);
		}

		@Nonnull
		@Override
		public List<String> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
		}
	}

}
