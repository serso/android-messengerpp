package org.solovyev.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.db.StringIdMapper;

import com.google.common.base.Predicate;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;

public class SqliteLinkedEntitiesDao<E> extends AbstractSQLiteHelper implements LinkedEntitiesDao<E> {

	@Nonnull
	private final Dao<E> dao;

	@Nonnull
	private final String tableName;

	@Nonnull
	private final String idColumnName;

	@Nonnull
	private final SqliteDaoEntityMapper<E> mapper;

	@Nonnull
	private final String linkedTableName;

	@Nonnull
	private final String linkedIdColumnName;

	@Nonnull
	private final String linkedEntityIdColumnName;

	public SqliteLinkedEntitiesDao(@Nonnull String tableName,
								   @Nonnull String idColumnName,
								   @Nonnull SqliteDaoEntityMapper<E> mapper,
								   @Nonnull Context context,
								   @Nonnull SQLiteOpenHelper sqliteOpenHelper,
								   @Nonnull String linkedTableName,
								   @Nonnull String linkedIdColumnName,
								   @Nonnull String linkedEntityIdColumnName) {
		super(context, sqliteOpenHelper);
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.mapper = mapper;
		this.linkedTableName = linkedTableName;
		this.linkedIdColumnName = linkedIdColumnName;
		this.linkedEntityIdColumnName = linkedEntityIdColumnName;
		this.dao = new SqliteDao<E>(tableName, idColumnName, mapper, context, sqliteOpenHelper);
	}

	public long create(@Nonnull E entity) {
		return dao.create(entity);
	}

	@Override
	@Nullable
	public E read(@Nonnull String id) {
		return dao.read(id);
	}

	@Override
	@Nonnull
	public Collection<E> readAll() {
		return dao.readAll();
	}

	@Override
	@Nonnull
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	public long update(@Nonnull E entity) {
		return dao.update(entity);
	}

	public void delete(@Nonnull E entity) {
		dao.delete(entity);
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}

	@Override
	public void deleteAll() {
		dao.deleteAll();
	}

	@Nonnull
	@Override
	public MergeDaoResult<E, String> mergeLinkedEntities(@Nonnull String id, @Nonnull List<E> linkedEntities, boolean allowRemoval, boolean allowUpdate) {
		final MergeDaoResultImpl<E, String> result = new MergeDaoResultImpl<E, String>(linkedEntities);

		final List<String> idsFromDb = readLinkedEntityIds(id);
		for (final String idFromDb : idsFromDb) {
			try {
				// entity exists both in db and on remote server => just update entity properties
				final E updatedObject = find(linkedEntities, new Predicate<E>() {
					@Override
					public boolean apply(@Nullable E entity) {
						return entity != null && idFromDb.equals(mapper.getId(entity));
					}
				});

				if (allowUpdate) {
					result.addUpdatedObject(updatedObject);
				}
			} catch (NoSuchElementException e) {
				if (allowRemoval) {
					// entity was removed on remote server => need to remove from local db
					result.addRemovedObjectId(idFromDb);
				}
			}
		}

		final Collection<String> allIdsFromDb = readAllIds();
		for (E entity : linkedEntities) {
			try {
				// entity exists both in db and on remote server => case already covered above
				find(idsFromDb, equalTo(mapper.getId(entity)));
			} catch (NoSuchElementException e) {
				// entity was added on remote server => need to add to local db
				if (allIdsFromDb.contains(mapper.getId(entity))) {
					// only link must be added - entity already in entities table
					result.addAddedObjectLink(entity);
				} else {
					// no entity information in local db is available - full entity insertion
					result.addAddedObject(entity);
				}
			}
		}
		return result;
	}

	@Nonnull
	@Override
	public List<String> readLinkedEntityIds(@Nonnull String id) {
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
