package org.solovyev.android.messenger;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface LinkedEntitiesDao<E> {

	@Nonnull
	MergeDaoResult<E, String> mergeLinkedEntities(@Nonnull String id,
												  @Nonnull Iterable<E> linkedEntities,
												  boolean allowRemoval,
												  boolean allowUpdate);

	@Nonnull
	public Collection<String> readLinkedEntityIds(@Nonnull String id);
}
