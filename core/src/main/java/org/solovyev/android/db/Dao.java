package org.solovyev.android.db;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Dao<E> {

	void create(@Nonnull E entity);

	@Nullable
	E read(@Nonnull String id);

	@Nonnull
	Collection<E> readAll();

	void update(@Nonnull E entity);

	void delete(@Nonnull E entity);

	void deleteById(@Nonnull String id);
}
