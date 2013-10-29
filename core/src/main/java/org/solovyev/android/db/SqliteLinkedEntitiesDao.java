package org.solovyev.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.Mergeable;
import org.solovyev.android.messenger.db.StringIdMapper;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;

public class SqliteLinkedEntitiesDao<E extends Identifiable & Mergeable<E>> extends AbstractSQLiteHelper implements LinkedEntitiesDao<E> {

	@Nonnull
	private final Dao<E> dao;

	@Nonnull
	private final String tableName;

	@Nonnull
	private final String idColumnName;

	@Nonnull
	private final String linkedTableName;

	@Nonnull
	private final String linkedIdColumnName;

	@Nonnull
	private final String linkedEntityIdColumnName;

	public SqliteLinkedEntitiesDao(@Nonnull String tableName,
								   @Nonnull String idColumnName,
								   @Nonnull Context context,
								   @Nonnull SQLiteOpenHelper sqliteOpenHelper,
								   @Nonnull String linkedTableName,
								   @Nonnull String linkedIdColumnName,
								   @Nonnull String linkedEntityIdColumnName,
								   @Nonnull Dao<E> dao) {
		super(context, sqliteOpenHelper);
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.linkedTableName = linkedTableName;
		this.linkedIdColumnName = linkedIdColumnName;
		this.linkedEntityIdColumnName = linkedEntityIdColumnName;
		this.dao = dao;
	}

	@Nonnull
	@Override
	public MergeDaoResult<E, String> mergeLinkedEntities(@Nonnull String id, @Nonnull Iterable<E> linkedEntities, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResultImpl<E, String> result = new MergeDaoResultImpl<E, String>();

		for (E linkedEntity : linkedEntities) {
			final E linkedEntityFromDb = dao.read(linkedEntity.getId());
			if (linkedEntityFromDb == null) {
				result.addAddedObject(linkedEntity);
			} else {
				final E mergedLinkedEntity = linkedEntityFromDb.merge(linkedEntity);
				result.addUpdatedObject(mergedLinkedEntity);
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public Collection<String> readLinkedEntityIds(@Nonnull String id) {
		return doDbQuery(getSqliteOpenHelper(), new LoadEntityIdsById(getContext(), id, getSqliteOpenHelper()));
	}

	public final class LoadEntityIdsById extends AbstractDbQuery<List<String>> {

		@Nonnull
		private final String id;

		private LoadEntityIdsById(@Nonnull Context context, @Nonnull String id, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.id = id;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query(tableName, null, idColumnName + " in (select " + linkedEntityIdColumnName + " from " + linkedTableName + " where " + linkedIdColumnName + " = ? )", new String[]{id}, null, null, null);
		}

		@Nonnull
		@Override
		public List<String> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
		}
	}
}
