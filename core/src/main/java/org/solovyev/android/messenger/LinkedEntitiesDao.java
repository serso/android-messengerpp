package org.solovyev.android.messenger;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.db.Dao;

public interface LinkedEntitiesDao<E> extends Dao<E> {

	@Nonnull
	MergeDaoResult<E, String> mergeLinkedEntities(@Nonnull String id,
												  @Nonnull List<E> linkedEntities,
												  boolean allowRemoval,
												  boolean allowUpdate);

	@Nonnull
	public List<String> readLinkedEntityIds(@Nonnull String id);
}
